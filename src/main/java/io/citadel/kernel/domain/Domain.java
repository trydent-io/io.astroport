package io.citadel.kernel.domain;

import io.citadel.eventstore.data.Feed;
import io.citadel.kernel.domain.attribute.Attribute;
import io.citadel.kernel.domain.repository.Repository;
import io.citadel.kernel.domain.service.Defaults;
import io.citadel.kernel.eventstore.EventStore;
import io.citadel.kernel.func.ThrowableFunction;
import io.citadel.kernel.func.ThrowablePredicate;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.util.stream.Stream;

public sealed interface Domain {
  Defaults defaults = Defaults.Companion;

  sealed interface Verticle extends Domain, io.vertx.core.Verticle permits Defaults.Service {
  }

  interface State<S extends Enum<S>> {
  }

  interface Command {
  }

  interface Event {
    default JsonObject asJson() { return JsonObject.mapFrom(this); }
  }

  interface Snapshot<A extends Aggregate, M extends Record> {
    Snapshot<A, M> apply(String aggregateId, long aggregateVersion, String eventName, JsonObject eventData);

    default A aggregate() {
      return aggregate(m -> true);
    }

    A aggregate(ThrowablePredicate<? super M> predicate);
  }

  interface Aggregates<A extends Aggregate, I extends Domain.ID<?>, E extends Domain.Event, M extends Record> {
    static <A extends Aggregate, I extends Domain.ID<?>, E extends Domain.Event, M extends Record> Aggregates<A, I, E, M> repository(EventStore eventStore, Snapshot<A, M> snapshot, String name, ThrowableFunction<? super String, ? extends I> asId) {
      return new Repository<>(eventStore, snapshot, name);
    }

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
    static Transaction committable(EventStore eventStore) {
      return new Committable(eventStore, Stream.empty());
    }
    Transaction log(Domain.Event... events);

    Future<Void> commit(String aggregateId, String aggregateName, long aggregateVersion, String by);

    default Future<Void> commit(String aggregateId, String aggregateName, long aggregateVersion) {
      return commit(aggregateId, aggregateName, aggregateVersion, null);
    }
  }

  interface Seed<M> {
    <R> R eventually(ThrowableFunction<? super M, ? extends R> then);
  }

  interface ID<T> extends Attribute<T> {
  }

  interface Handler<A> extends io.vertx.core.Handler<Message<A>> {
    @Override
    default void handle(Message<A> message) {
      handle(message, message.headers().get("aggregateId"), message.body(), message.headers().get("by"), Headers.of(message.headers()));
    }

    void handle(final Message<A> message, final String aggregateId, final A content, final String by, final Headers headers);
  }
}

final class Committable implements Domain.Transaction {
  private final EventStore eventStore;
  private final Stream<Domain.Event> events;

  Committable(EventStore eventStore, Stream<Domain.Event> events) {
    this.eventStore = eventStore;
    this.events = events;
  }
  public Domain.Transaction log(Domain.Event... events) {
    return new Committable(eventStore, this.events != null
      ? Stream.concat(this.events, Stream.of(events))
      : Stream.of(events)
    );
  }

  @Override
  public Future<Void> commit(String aggregateId, String aggregateName, long aggregateVersion, String by) {
    return eventStore.feed(
      new Feed.Aggregate(aggregateId, aggregateName, aggregateVersion),
      events.map(it -> new Feed.Event(it.getClass().getSimpleName(), JsonObject.mapFrom(it))),
      by
    ).mapEmpty();
  }
}
