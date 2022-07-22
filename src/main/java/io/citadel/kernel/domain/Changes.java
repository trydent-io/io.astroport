package io.citadel.kernel.domain;

import io.citadel.kernel.eventstore.event.Entity;
import io.citadel.kernel.eventstore.event.EntityEvent;
import io.citadel.kernel.eventstore.event.Event;
import io.citadel.kernel.lang.Arrayable;
import io.citadel.kernel.media.Json;

import java.util.Iterator;
import java.util.stream.Stream;

interface Events<E> {
  E[] events();
  Entity entity();

  default Iterator<EntityEvent> iter() {
    return Stream.of(events())
      .map(it -> Event.unsaved(it.getClass().getSimpleName(), Json.fromAny(it)))
      .map(it -> EntityEvent.change(entity(), it))
      .iterator();
  }
}

@SuppressWarnings("unchecked")
public sealed interface Changes<E> extends Iterable<EntityEvent> {

  static <E, S extends Enum<S> & State<S, E>> Changes<E> of(Entity entity, S current) {
    return new Stateful<>(entity, current);
  }

  Changes<E> append(E event);

  record Stateful<E, S extends Enum<S> & State<S, E>>(Entity entity, S state, E... events) implements Changes<E>, Arrayable<E>, Events<E> {
    @Override
    public Iterator<EntityEvent> iterator() {
      return iter();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Changes<E> append(E event) {
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
    public Iterator<EntityEvent> iterator() {
      return iter();
    }
  }
}
