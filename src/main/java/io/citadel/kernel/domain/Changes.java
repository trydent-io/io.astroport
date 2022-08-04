package io.citadel.kernel.domain;

import io.citadel.kernel.eventstore.Audit;
import io.citadel.kernel.eventstore.EventStore;
import io.citadel.kernel.lang.Arrayable;
import io.vertx.core.Future;

import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public interface Changes<EVENT extends Record> {
  static <EVENT extends Record, STATE extends Enum<STATE> & State<STATE, EVENT>> Changes<EVENT> of(EventStore eventStore, Audit.Entity entity, STATE current) {
    return new Stateful<>(eventStore, entity, current);
  }

  Changes<EVENT> append(EVENT event);
  Future<Void> apply();

  final class Stateful<EVENT extends Record, STATE extends Enum<STATE> & State<STATE, EVENT>> implements Changes<EVENT>, Arrayable<EVENT> {
    private final EventStore eventStore;
    private final Audit.Entity entity;
    private final STATE state;
    private final EVENT[] events;

    Stateful(EventStore eventStore, Audit.Entity entity, STATE state, EVENT... events) {
      this.eventStore = eventStore;
      this.entity = entity;
      this.state = state;
      this.events = events;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Changes<EVENT> append(EVENT event) {
      if (!state.transitable(event)) throw new IllegalStateException("Can't transit state %s with event %s".formatted(state, event));

      return new Stateful<>(eventStore, entity, state.transit(event), append(events, event));
    }

    @Override
    public Future<Void> apply() {
      return eventStore.store(Stream.of(events).map(event -> Audit.of(entity, Audit.Event.from(event))));
    }
  }
}


