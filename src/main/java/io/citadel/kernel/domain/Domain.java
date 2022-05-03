package io.citadel.kernel.domain;

import io.citadel.eventstore.data.Feed;
import io.citadel.kernel.domain.attribute.Attribute;
import io.citadel.kernel.domain.service.Defaults;
import io.citadel.kernel.eventstore.EventStore;
import io.citadel.kernel.func.ThrowablePredicate;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.util.stream.Stream;

public sealed interface Domain {
  Defaults defaults = Defaults.Companion;

  sealed interface Verticle extends Domain, io.vertx.core.Verticle permits Defaults.Service {}

  interface State<S extends Enum<S>> {}
  interface Command {}
  interface Event {
    default Feed.Event asFeed() { return new Feed.Event(this.getClass().getSimpleName(), JsonObject.mapFrom(this)); }
  }

  interface Snapshot<A extends Aggregate, M extends Record> {
    Snapshot<A, M> apply(String aggregateId, long aggregateVersion, String eventName, JsonObject eventData);

    default A aggregate(final Transaction transaction) {
      return aggregate(m -> true);
    }

    A aggregate(ThrowablePredicate<? super M> predicate);
  }

  interface Aggregates<A extends Aggregate, I extends Domain.ID<?>, M extends Record> {
    Future<A> lookup(I id);
    Future<A> lookup(I id, ThrowablePredicate<? super M> with);
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

  interface Handler<A> extends io.vertx.core.Handler<Message<A>> {
    @Override
    default void handle(Message<A> message) {
      handle(message, message.headers().get("aggregateId"), message.body(), message.headers().get("by"), Headers.of(message.headers()));
    }

    void handle(final Message<A> message, final String aggregateId, final A content, final String by, final Headers headers);
  }
}

final class Changes implements Domain.Transaction {
  private final EventStore eventStore;
  private final Stream<Domain.Event> events;

  Changes(EventStore eventStore, Stream<Domain.Event> events) {
    this.eventStore = eventStore;
    this.events = events;
  }
  public Domain.Transaction log(Domain.Event... events) {
    return new Changes(eventStore, this.events != null
      ? Stream.concat(this.events, Stream.of(events))
      : Stream.of(events)
    );
  }

  @Override
  public Future<Void> commit(String aggregateId, String aggregateName, long aggregateVersion, String by) {
    return eventStore.feed(
      new Feed.Aggregate(aggregateId, aggregateName, aggregateVersion),
      events.map(Domain.Event::asFeed),
      by
    ).mapEmpty();
  }
}
