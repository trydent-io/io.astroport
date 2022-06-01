package io.citadel.kernel.eventstore;

import io.citadel.kernel.domain.Domain;
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

  <ID extends Domain.ID<?>> Future<Snapshot<ID>> findSnapshot(ID aggregateId, String aggregateName, long aggregateVersion);
  default <ID extends Domain.ID<?>> Future<Snapshot<ID>> findSnapshot(ID aggregateId, String aggregateName) {
    return findSnapshot(aggregateId, aggregateName, -1);
  }
  default <ID extends Domain.ID<?>> Future<Snapshot<ID>> findSnapshot(ID aggregateId) {
    return findSnapshot(aggregateId, null);
  }

  final class Snapshot<ID extends Domain.ID<?>> {
    private final Meta.Aggregate<ID> aggregate;
    private final Stream<Meta.Event> events;
    private final Vertx vertx;
    private final SqlClient sqlClient;

    public Snapshot(ID aggregateId, String aggregateName, long aggregateVersion, Vertx vertx, SqlClient sqlClient) {
      this(new Meta.Aggregate<>(aggregateId, aggregateName, aggregateVersion), Stream.empty(), vertx, sqlClient);
    }

    private Snapshot(Meta.Aggregate<ID> aggregate, Stream<Meta.Event> events, Vertx vertx, SqlClient sqlClient) {
      this.aggregate = aggregate;
      this.events = events;
      this.vertx = vertx;
      this.sqlClient = sqlClient;
    }

    Snapshot<ID> aggregate(Meta.Aggregate<ID> aggregate) {
      return this.aggregate.version() == -1 ? new Snapshot<>(aggregate, events,vertx, sqlClient) : this;
    }

    Snapshot<ID> append(Meta.Event event) {
      return new Snapshot<>(aggregate, Stream.concat(events, Stream.of(event)), vertx, sqlClient);
    }

    public <M extends Record & Domain.Model<ID>, E extends Domain.Event> Normalize<ID, M, E> normalize(ThrowableBiFunction<? super String, ? super JsonObject, ? extends E> converter) {
      return initializer -> new Archetype<>(
        initializer.apply(aggregate.id()),
        aggregate.name(),
        aggregate.version(),
        events.map(event -> converter.apply(event.name(), event.data()))
      );
    }

    private final class Archetype<M extends Record & Domain.Model<ID>, E extends Domain.Event> implements Identity<M, E> {
      private static final Set<Collector.Characteristics> IdentityFinish = Set.of(IDENTITY_FINISH);

      private final M model;
      private final String name;
      private final long version;
      private final Stream<E> events;

      Archetype(M model, String name, long version, Stream<E> events) {
        this.model = model;
        this.name = name;
        this.version = version;
        this.events = events;
      }

      @Override
      public <S extends Enum<S> & Domain.State<S, E>> Context<M, E> hydrate(final S initial, final ThrowableBiFunction<? super M, ? super E, ? extends M> hydrator) {
        return events.collect(asContext(initial, hydrator));
      }

      private <S extends Enum<S> & Domain.State<S, E>> AsContext<S> asContext(S initial, ThrowableBiFunction<? super M, ? super E, ? extends M> hydrator) {
        return new AsContext<>(initial, hydrator);
      }

      @SuppressWarnings({"unchecked", "ConstantConditions"})
      private final class AsContext<S extends Enum<S> & Domain.State<S, E>> implements Collector<E, AsContext<S>.Staging[], Context<M, E>> {
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
        public Function<Staging[], Context<M, E>> finisher() {
          return stagings -> new Context<>(stagings[0].model, name, version, Transaction.open(vertx, sqlClient, stagings[0].state));
        }

        @Override
        public Set<Characteristics> characteristics() {
          return IdentityFinish;
        }
      }
    }
  }
}
