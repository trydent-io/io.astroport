package io.citadel.kernel.domain;

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
  sealed interface Verticle extends Domain, io.vertx.core.Verticle permits Defaults.Service {}

  interface State<S extends Enum<S>> {}

  interface Command {}
  interface Event {}

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
    default Future<A> persist(I id, long version, Stream<E> events) {
      return persist(id, version, events, null);
    }

    Future<A> lookup(I id, ThrowablePredicate<? super M> with);

    Future<A> persist(I id, long version, Stream<E> events, String by);
  }

  interface Aggregate {
    <T> T commit();
  }
  interface Seed<M> {
    <R> R eventually(ThrowableFunction<? super M, ? extends R> then);
  }

  interface ID<T> extends Attribute<T> {}

  interface Handler<A> extends io.vertx.core.Handler<Message<A>> {
    @Override
    default void handle(Message<A> message) {
      handle(message, message.body(), Headers.of(message.headers()));
    }

    void handle(final Message<A> message, final A content, final Headers headers);
  }
}

