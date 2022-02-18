package io.citadel.kernel.domain;

import io.citadel.kernel.eventstore.EventLog;
import io.citadel.kernel.media.ArrayIterator;
import io.vertx.core.json.JsonObject;

import java.util.Iterator;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

public sealed interface Domain {
  enum Namespace implements Domain {}

  interface Command<E extends Event<?>> {
    E asEvent();
  }

  interface Event<C extends Command<?>> {
    C asCommand();

    default EventLog asEventLog(UUID aggregateId, String aggregateName, UUID revision) {
      return EventLog.of(this.getClass().getSimpleName(), JsonObject.mapFrom(this), aggregateId, aggregateName, revision);
    }
  }

  interface Aggregate<C extends Domain.Command<E>, E extends Domain.Event<C>, A extends Aggregate<C, E, A>> extends Iterable<E>, Function<C, A> {
    @Override
    default Iterator<E> iterator() { return new ArrayIterator<>(); }

    @SuppressWarnings("unchecked")
    default A flush() { return (A) this; }
  }

  interface ID<T> extends Supplier<T> {}
}
