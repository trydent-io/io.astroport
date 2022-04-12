package io.citadel.kernel.domain;

import io.citadel.kernel.domain.eventstore.EventStore;
import io.citadel.kernel.domain.eventstore.data.AggregateInfo;
import io.citadel.kernel.domain.eventstore.data.EventInfo;
import io.citadel.kernel.domain.attribute.Attribute;
import io.citadel.kernel.domain.repository.Repository;
import io.citadel.kernel.domain.service.Defaults;
import io.citadel.kernel.func.ThrowableBiFunction;
import io.citadel.kernel.func.ThrowableFunction;
import io.citadel.kernel.media.Json;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;

import java.util.stream.Stream;

public sealed interface Domain {
  Defaults defaults = Defaults.Companion;

  interface State<S extends Enum<S>> {}


  interface Handler<R extends Record> extends io.vertx.core.Handler<Message<R>> {
    @Override
    default void handle(Message<R> message) {
      handle(message, message.body(), Headers.of(message.headers()));
    }

    void handle(Message<R> message, R content, Headers headers);
  }

  interface Command {}
  interface Event {
    default EventInfo asInfo() {
      return new EventInfo(getClass().getSimpleName(), Json.with(this));
    }
  }

  interface Aggregates<A extends Aggregate, I extends Domain.ID> {
    static <A extends Aggregate, I extends Domain.ID> Aggregates<A, I> repository(EventStore eventStore, Domain.Snapshot<A> snapshot, String name) {
      return new Repository<>(eventStore, snapshot, name);
    }

    Future<A> findBy(I id);
    default Future<Void> persist(AggregateInfo aggregate, Stream<EventInfo> events) {
      return persist(aggregate, events, null);
    }
    Future<Void> persist(AggregateInfo aggregate, Stream<EventInfo> events, String by);
  }

  interface Snapshot<A extends Aggregate> {
    A aggregate(String id, long version, Stream<EventInfo> events) throws Throwable;
  }
  interface Aggregate<S extends State<S>> {
    boolean is(S state);
    <T> T commit(ThrowableBiFunction<? super AggregateInfo, ? super Stream<EventInfo>, ? extends T> transaction);
  }
  interface Lifecycle<M> {
    <R> R eventually(ThrowableFunction<? super M, ? extends R> then);
    default M eventually() {
      return eventually(it -> it);
    }
  }

  interface ID extends Attribute<String> {}

  sealed interface Verticle extends Domain, io.vertx.core.Verticle permits Defaults.Service {}
  sealed interface Bus {

  }
}

