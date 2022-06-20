package io.citadel.kernel.eventstore;

import io.citadel.kernel.domain.State;
import io.citadel.kernel.eventstore.meta.*;
import io.citadel.kernel.func.ThrowableBiFunction;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlClient;

import java.util.Set;
import java.util.function.*;
import java.util.stream.Collector.Characteristics;
import java.util.stream.Stream;

import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;

public sealed interface Lookup<R> permits Sql {
  static Lookup<Meta.Log> create(Vertx vertx, SqlClient client) {
    return new Sql(vertx, client);
  }

  default <T> Future<R> find(T aggregateId, String aggregateName, long aggregateVersion) {
    return find(Entity.id(aggregateId), Entity.name(aggregateName), Entity.version(aggregateVersion));
  }

  Future<R> find(ID id, Name name, Version version);

  final class Feed {
    private final Vertx vertx;
    private final SqlClient sqlClient;
    private final Entity entity;
    private final Stream<Event> events;

    public Feed(Vertx vertx, SqlClient sqlClient, Entity entity) {
      this(vertx, sqlClient, entity, Stream.empty());
    }

    private Feed(Vertx vertx, SqlClient sqlClient, Entity entity, Stream<Event> events) {
      this.vertx = vertx;
      this.sqlClient = sqlClient;
      this.entity = entity;
      this.events = events;
    }

    Feed aggregate(Entity entity) {
      return this.entity.version().isDefault() ? new Feed(vertx, sqlClient, entity, events) : this;
    }

    Feed append(Event event) {
      return new Feed(vertx, sqlClient, entity, Stream.concat(events, Stream.of(event)));
    }

    public <R extends Record, E> Transient<R, E> deserializes(ThrowableBiFunction<? super String, ? super JsonObject, ? extends E> converter) {
      return initializer -> new Detached<>(
        initializer.apply(entity.id()),
        entity,
        events.map(event -> converter.apply(event.name(), event.data()))
      );
    }

    private final class Detached<M extends Record, E> implements Identity<M, E> {
      private static final Set<Characteristics> IdentityFinish = Set.of(IDENTITY_FINISH);

      private final M model;
      private final Entity entity;
      private final Stream<E> events;

      private Detached(M model, Entity entity, Stream<E> events) {
        this.model = model;
        this.entity = entity;
        this.events = events;
      }


      @Override
      public <S extends Enum<S> & State<S, E>> Context<M, S, E> hydrate(final S initial, final ThrowableBiFunction<? super M, ? super E, ? extends M> hydrator) {
        return events.collect(asContext(initial, hydrator));
      }

      private <S extends Enum<S> & State<S, E>> AsContext<S> asContext(S initial, ThrowableBiFunction<? super M, ? super E, ? extends M> hydrator) {
        return new AsContext<>(initial, hydrator);
      }

      @SuppressWarnings({"unchecked", "ConstantConditions"})
      private final class AsContext<S extends Enum<S> & State<S, E>> implements Aggregator<E, AsContext<S>.Staging[], Context<M, S, E>> {
        private final class Staging {
          private M model;
          private S state;
          private Staging(M model, S initial) {
            this.model = model;
            this.state = initial;
          }
          private Staging apply(E event) {
            if (state != null) state = state.transit(event);
            model = hydrator.apply(model, event);
            return this;
          }
        }

        private final S initial;
        private final ThrowableBiFunction<? super M, ? super E, ? extends M> hydrator;

        private AsContext(final S initial, final ThrowableBiFunction<? super M, ? super E, ? extends M> hydrator) {
          this.initial = initial;
          this.hydrator = hydrator;
        }

        @Override
        public Supplier<Staging[]> supplier() {
          return () -> (Staging[]) new Object[]{ new Staging(model, initial)};
        }

        @Override
        public BiConsumer<Staging[], E> accumulator() {
          return (aggregates, event) -> aggregates[0] = aggregates[0].apply(event);
        }
        @Override
        public Function<Staging[], Context<M, S, E>> finisher() {
          return stagings -> new Context<>(stagings[0].model, stagings[0].state, Transaction.open(vertx, sqlClient, entity));
        }
      }
    }
  }
}
