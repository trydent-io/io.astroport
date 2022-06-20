package io.citadel.kernel.eventstore;

import io.citadel.kernel.domain.State;
import io.citadel.kernel.eventstore.meta.*;
import io.citadel.kernel.func.ThrowableBiFunction;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlClient;

import java.util.Objects;
import java.util.Set;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collector.Characteristics;
import java.util.stream.Stream;

import static java.util.Objects.*;
import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;

public sealed interface Lookup permits Sql {
  static Lookup create(Vertx vertx, SqlClient client) {
    return new Sql(vertx, client);
  }

  default <T> Future<Snapshot> find(T aggregateId, String aggregateName, long aggregateVersion) {
    return find(Aggregate.id(aggregateId), Aggregate.name(aggregateName), Aggregate.version(aggregateVersion));
  }

  Future<Snapshot> find(ID aggregateId, Name name, Version version);

  final class Snapshot {
    private final Vertx vertx;
    private final SqlClient sqlClient;
    private final Aggregate aggregate;
    private final Stream<Event> events;

    public Snapshot(Vertx vertx, SqlClient sqlClient, Aggregate aggregate) {
      this(vertx, sqlClient, aggregate, Stream.empty());
    }

    private Snapshot(Vertx vertx, SqlClient sqlClient, Aggregate aggregate, Stream<Event> events) {
      this.vertx = vertx;
      this.sqlClient = sqlClient;
      this.aggregate = aggregate;
      this.events = events;
    }

    Snapshot aggregate(Aggregate aggregate) {
      return this.aggregate.version().isDefault() ? new Snapshot(vertx, sqlClient, aggregate, events) : this;
    }

    Snapshot append(Event event) {
      return new Snapshot(vertx, sqlClient, aggregate, Stream.concat(events, Stream.of(event)));
    }

    public <R extends Record, E> Transient<R, E> deserializes(ThrowableBiFunction<? super String, ? super JsonObject, ? extends E> converter) {
      return initializer -> new Detached<>(
        initializer.apply(aggregate.id()),
        aggregate,
        events.map(event -> converter.apply(event.name(), event.data()))
      );
    }

    private final class Detached<M extends Record, E> implements Identity<M, E> {
      private static final Set<Characteristics> IdentityFinish = Set.of(IDENTITY_FINISH);

      private final M model;
      private final Aggregate aggregate;
      private final Stream<E> events;

      private Detached(M model, Aggregate aggregate, Stream<E> events) {
        this.model = model;
        this.aggregate = aggregate;
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
          return stagings -> new Context<>(stagings[0].model, stagings[0].state, Transaction.open(vertx, sqlClient, aggregate));
        }
      }
    }
  }
}
