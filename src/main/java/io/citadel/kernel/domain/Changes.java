package io.citadel.kernel.domain;

import io.citadel.kernel.eventstore.event.Entity;
import io.citadel.kernel.eventstore.event.Audit;
import io.citadel.kernel.eventstore.event.Event;
import io.citadel.kernel.lang.Arrayable;
import io.citadel.kernel.media.Json;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static io.citadel.kernel.eventstore.event.Event.data;
import static io.citadel.kernel.eventstore.event.Event.name;

@SuppressWarnings("unchecked")
public sealed interface Changes<EVENT> extends Iterable<Audit> {
  static <EVENT, STATE extends Enum<STATE> & State<STATE, EVENT>> Changes<EVENT> of(Entity entity, STATE current) {
    return new Stateful<>(entity, current);
  }

  Changes<EVENT> append(EVENT event);

  default Stream<Audit> stream() {
    return StreamSupport.stream(spliterator(), false);
  }

  record Stateful<EVENT, STATE extends Enum<STATE> & State<STATE, EVENT>>(Entity entity, STATE state, EVENT... events) implements Changes<EVENT>, Arrayable<EVENT>, Events<EVENT> {
    @Override
    public Iterator<Audit> iterator() {
      return iter();
    }
    @SuppressWarnings("unchecked")
    @Override
    public Changes<EVENT> append(EVENT event) {
      if (!state.transitable(event)) throw new IllegalStateException("Can't transit state %s with event %s".formatted(state, event));

      return new Stateful<>(entity, state.transit(event), append(events, event));
    }
  }
  record Stateless<E>(Entity entity, E... events) implements Changes<E>, Arrayable<E>, Events<E> {
    @Override
    public Changes<E> append(E event) {
      return new Stateless<>(entity, append(events, event));
    }
    @Override
    public Iterator<Audit> iterator() {
      return iter();
    }
  }
}
interface Events<EVENT> {
  EVENT[] events();
  Entity entity();
  default Iterator<Audit> iter() {
    return Stream.of(events())
      .map(it ->
        Event.of(
          name(it.getClass().getSimpleName()),
          data(Json.fromAny(it))
        )
      )
      .map(event -> Audit.of(entity(), event))
      .iterator();
  }
}


