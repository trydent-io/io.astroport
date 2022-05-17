package io.citadel.kernel.domain;

import io.citadel.eventstore.data.Feed;
import io.citadel.kernel.domain.attribute.Attribute;
import io.citadel.kernel.domain.model.Defaults;
import io.citadel.kernel.domain.model.Service;
import io.citadel.kernel.eventstore.EventStore;
import io.citadel.kernel.func.ThrowableFunction;
import io.citadel.kernel.func.ThrowablePredicate;
import io.citadel.kernel.vertx.Task;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.util.function.Predicate;

public sealed interface Domain {
  Defaults defaults = Defaults.Companion;

  sealed interface Verticle extends Domain, io.vertx.core.Verticle permits Service {}

  interface State<S extends Enum<S>> {}
  interface Command {}
  interface Event {
    default Feed.Event asFeed() { return new Feed.Event(this.getClass().getSimpleName(), JsonObject.mapFrom(this)); }
  }

  interface Snapshot<M extends Record & Domain.Model<?>, A extends Aggregate>  {
    Snapshot<M, A> apply(String aggregateId, long aggregateVersion, String eventName, JsonObject eventData);
    A aggregate(EventStore eventStore);
    A aggregate(EventStore eventStore, Predicate<? super M> verify);
  }

  interface Model<ID extends Domain.ID<?>> {
    ID id();
  }

  interface Lookup<M extends Record & Model<?>, A extends Aggregate> {
    Future<A> findAggregate(Domain.ID<?> id, ThrowablePredicate<? super M> verify);
  }

  interface Aggregate<M extends Record & Model<?>, A extends Aggregate<M, A>> {
    Future<A> filter(ThrowablePredicate<? super M> predicate);
    Future<A> map(ThrowableFunction<? super M, ? extends >)

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
}

