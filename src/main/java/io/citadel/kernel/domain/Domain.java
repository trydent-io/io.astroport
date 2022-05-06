package io.citadel.kernel.domain;

import io.citadel.eventstore.data.Feed;
import io.citadel.kernel.domain.attribute.Attribute;
import io.citadel.kernel.domain.model.Defaults;
import io.citadel.kernel.domain.model.Service;
import io.citadel.kernel.eventstore.EventStore;
import io.citadel.kernel.func.ThrowablePredicate;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public sealed interface Domain {
  Defaults defaults = Defaults.Companion;

  sealed interface Verticle extends Domain, io.vertx.core.Verticle permits Service {}

  interface State<S extends Enum<S>> {}
  interface Command {}
  interface Event {
    default Feed.Event asFeed() { return new Feed.Event(this.getClass().getSimpleName(), JsonObject.mapFrom(this)); }
  }

  interface Snapshot<M extends Record & Domain.Model<?>, A extends Aggregate<M>, S extends Snapshot<M, A, S>> {
    S identity(String id);
    S event(long aggregateVersion, String eventName, JsonObject eventData);
    A aggregate(EventStore eventStore);
  }

  interface Model<ID extends Domain.ID<?>> {
    ID id();
  }

  interface Aggregates<ID extends Domain.ID<?>, M extends Record & Model<ID>, A extends Aggregate<M>> {
    Future<A> lookup(ID id);
    Future<A> lookup(ID id, ThrowablePredicate<? super M> predicate);
  }

  interface Aggregate<M extends Model<?>> {
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

  interface Handler<A> extends io.vertx.core.Handler<Message<A>> {
    @Override
    default void handle(Message<A> message) {
      handle(message, message.headers().get("aggregateId"), message.body(), message.headers().get("by"), Headers.of(message.headers()));
    }

    void handle(final Message<A> message, final String aggregateId, final A content, final String by, final Headers headers);
  }
}

