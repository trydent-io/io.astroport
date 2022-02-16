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

  interface Command {
    default Event[] asEvents() {
      return new Event[] {asEvent()};
    }

    Event asEvent();
  }

  interface Event {
    Command asCommand();

    default EventLog asEventLog(UUID aggregateId, String aggregateName, UUID revision) {
      return EventLog.of(this.getClass().getSimpleName(), JsonObject.mapFrom(this), aggregateId, aggregateName, revision);
    }
  }

  interface State<E extends Enum<E> & State<E>> {}

  interface Aggregate<C extends Domain.Command, S extends State<?>, A extends Aggregate<C, S, A>> extends Iterable<Domain.Event>, Function<C, A> {
    @Override
    default Iterator<Domain.Event> iterator() { return new ArrayIterator<>(); }

    @SuppressWarnings("unchecked")
    A flush();

    default boolean is(S state) {
      return false;
    }
  }

  interface ID<T> extends Supplier<T> {}
}
