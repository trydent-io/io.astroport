package io.citadel.kernel.domain;

import io.citadel.eventstore.data.Feed;
import io.citadel.kernel.domain.attribute.Attribute;
import io.citadel.kernel.domain.model.CollectorImpl;
import io.citadel.kernel.domain.model.Defaults;
import io.citadel.kernel.domain.model.Service;
import io.citadel.kernel.eventstore.EventStore;
import io.citadel.kernel.func.ThrowablePredicate;
import io.citadel.kernel.vertx.Task;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static io.citadel.kernel.func.ThrowableBiFunction.noOp;
import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;

public sealed interface Domain {
  Defaults defaults = Defaults.Companion;

  sealed interface Verticle extends Domain, io.vertx.core.Verticle permits Service {}

  interface State<S extends Enum<S>> {}
  interface Command {}
  interface Event {
    default Feed.Event asFeed() { return new Feed.Event(this.getClass().getSimpleName(), JsonObject.mapFrom(this)); }
  }

  static <A extends Aggregate> Collector<Feed.Entry, ?, A> toAggregate() {
    BinaryOperator<A> op = null;
    return new CollectorImpl<>(
      () -> (A[]) new Object[] { null },
      (a, t) -> { a[0] = op.apply(a[0], t); },
      (a, b) -> { a[0] = op.apply(a[0], b[0]); return a; },
      a -> a[0],
      IDENTITY_FINISH);
  }

  @SuppressWarnings("unchecked")
  private static <T> Supplier<T[]> boxSupplier(T identity) {
    return ;
  }

  interface Snapshot<A extends Aggregate> extends Collector<Feed.Entry, Domain.Snapshot<A>, Optional<A>> {
    Set<Characteristics> characteristics = Set.of(IDENTITY_FINISH);
    @Override
    default Supplier<Snapshot<A>> supplier() {
      return () -> this;
    }

    @Override
    default BinaryOperator<Snapshot<A>> combiner() {
      return noOp();
    }

    @Override
    default Set<Characteristics> characteristics() {
      return characteristics;
    }
  }

  interface Model<ID extends Domain.ID<?>> {
    ID id();
  }

  interface Lookup<M extends Record & Model<?>, A extends Aggregate> {
    default Future<A> findAggregate(Domain.ID<?> id) {
      return findAggregate(id, it -> true);
    }
    Future<A> findAggregate(Domain.ID<?> id, ThrowablePredicate<? super M> verify);
  }

  interface Aggregate {
    default Future<Void> submit() {
      return submit(null);
    }
    Future<Void> submit(String by);
  }

  interface Transaction {
    Transaction log(Domain.Event... events);

    Future<Void> commit(String aggregateId, String aggregateName, long aggregateVersion, String by);

    default Future<Void> commit(String aggregateId, String aggregateName, long aggregateVersion) {
      return commit(aggregateId, aggregateName, aggregateVersion, null);
    }
  }

  interface ID<T> extends Attribute<T> {}

  interface Handler<S extends Record> extends Task.Handler<S> {
    @Override
    default void handle(Message<S> message) {
      handle(message, message.headers().get("aggregateId"), message.body(), message.headers().get("by"), Headers.of(message.headers()));
    }

    void handle(final Message<S> message, final String aggregateId, final S content, final String by, final Headers headers);
  }
}

