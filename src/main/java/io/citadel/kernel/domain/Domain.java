package io.citadel.kernel.domain;

import io.citadel.domain.forum.handler.Commands;
import io.citadel.kernel.domain.attribute.Attribute;
import io.citadel.kernel.domain.repository.Repository;
import io.citadel.kernel.domain.service.Defaults;
import io.citadel.kernel.eventstore.EventStore;
import io.citadel.kernel.func.ThrowableBiFunction;
import io.citadel.kernel.func.ThrowableFunction;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.util.stream.Stream;

public sealed interface Domain {
  Defaults defaults = Defaults.Companion;
  sealed interface Verticle extends io.vertx.core.Verticle permits Defaults.Service {}

  interface State<S extends Enum<S>> {}

  interface Command {}
  interface Event {}

  interface Snapshot<A extends Aggregate, I extends Domain.ID<?>> {
    Snapshot<A, I> apply(I aggregateId, long aggregateVersion, String eventName, JsonObject eventData);
    A aggregate();
  }

  interface Aggregates<A extends Aggregate, I extends Domain.ID<?>, E extends Domain.Event> {
    static <A extends Aggregate, I extends Domain.ID<?>, E extends Domain.Event> Aggregates<A, I, E> repository(EventStore eventStore, Snapshot<A> snapshot, String name, ThrowableFunction<? super String, ? extends I> asId) {
      return new Repository<>(eventStore, snapshot, name, asId);
    }

    Future<A> lookup(I id);
    default Future<A> persist(I id, long version, Stream<E> events) {
      return persist(id, version, events, null);
    }
    Future<A> persist(I id, long version, Stream<E> events, String by);
  }

  interface Aggregate {
    <T> T commit(ThrowableBiFunction<? super AggregateInfo, ? super Stream<EventInfo>, ? extends T> transaction);
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

