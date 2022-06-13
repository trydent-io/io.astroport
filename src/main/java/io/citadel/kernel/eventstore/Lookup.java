package io.citadel.kernel.eventstore;

import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.eventstore.meta.*;
import io.citadel.kernel.func.ThrowableBiFunction;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlClient;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;

public sealed interface Lookup permits Snapshots {
  static Lookup create(Vertx vertx, SqlClient client) {
    return new Snapshots(vertx, client);
  }
  default <T> Future<Snapshot> findSnapshot(T aggregateId, String aggregateName, long aggregateVersion) {
    return findSnapshot(Aggregate.id(aggregateId), Aggregate.name(aggregateName), Aggregate.version(aggregateVersion));
  }
  Future<Snapshot> findSnapshot(ID<?> aggregateId, Name name, Version version);

  final class Snapshot {
    private final Aggregate aggregate;
    private final Stream<Event> events;
    private final Vertx vertx;
    private final SqlClient sqlClient;

    public Snapshot(Vertx vertx, SqlClient sqlClient, Aggregate aggregate) {
      this(vertx, sqlClient, aggregate, Stream.empty());
    }
    private Snapshot(Vertx vertx, SqlClient sqlClient, Aggregate aggregate, Stream<Event> events) {
      this.aggregate = aggregate;
      this.events = events;
      this.vertx = vertx;
      this.sqlClient = sqlClient;
    }

    Snapshot aggregate(Aggregate aggregate) {
      return this.aggregate.version().isDefault() ? new Snapshot(vertx, sqlClient, aggregate, events) : this;
    }

    Snapshot append(Event event) {
      return new Snapshot(vertx, sqlClient, aggregate, Stream.concat(events, Stream.of(event)));
    }

    public <R extends Record, E> Normalize<R, E> normalize(ThrowableBiFunction<? super String, ? super JsonObject, ? extends E> converter) {
      return initializer -> new Archetype<>(
        initializer.apply(aggregate.id()),
        aggregate,
        events.map(event -> converter.apply(event.name(), event.data()))
      );
    }

    private final class Archetype<M extends Record, E> implements Identity<M, E> {
      private static final Set<Collector.Characteristics> IdentityFinish = Set.of(IDENTITY_FINISH);

      private final M model;
      private final Aggregate aggregate;
      private final Stream<E> events;

      private Archetype(M model, Aggregate aggregate, Stream<E> events) {
        this.model = model;
        this.aggregate = aggregate;
        this.events = events;
      }


      @Override
      public <S extends Enum<S> & Domain.State<S, E>> Context<M, S, E> hydrate(final S initial, final ThrowableBiFunction<? super M, ? super E, ? extends M> hydrator) {
        return events.collect(asContext(initial, hydrator));
      }

      private <S extends Enum<S> & Domain.State<S, E>> AsContext<S> asContext(S initial, ThrowableBiFunction<? super M, ? super E, ? extends M> hydrator) {
        return new AsContext<>(initial, hydrator);
      }

      @SuppressWarnings({"unchecked", "ConstantConditions"})
      private final class AsContext<S extends Enum<S> & Domain.State<S, E>> implements Collector<E, AsContext<S>.Staging[], Context<M, S, E>> {
        private final class Staging {
          private final M model;
          private final S state;
          private Staging(M model) {
            this(model, null);
          }
          private Staging(M model, S state) {
            this.model = model;
            this.state = state;
          }
          private Staging apply(E event) {
            return state == null
              ? new Staging(hydrator.apply(model, event))
              : state.next(event)
              .map(next -> new Staging(hydrator.apply(model, event), next))
              .orElseThrow(() -> new IllegalStateException("Can't apply event, since current state is %s and event is %s".formatted(state, event)));
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
          return () -> (Staging[]) new Object[]{new Staging(model, initial)};
        }

        @Override
        public BiConsumer<Staging[], E> accumulator() {
          return (aggregates, event) -> aggregates[0] = aggregates[0].apply(event);
        }

        @Override
        public BinaryOperator<Staging[]> combiner() {
          return (stagings, stagings2) -> stagings;
        }

        @Override
        public Function<Staging[], Context<M, S, E>> finisher() {
          return stagings -> new Context<>(stagings[0].model, stagings[0].state, Transaction.open(vertx, sqlClient, aggregate));
        }

        @Override
        public Set<Characteristics> characteristics() {
          return IdentityFinish;
        }
      }
    }
  }
}
