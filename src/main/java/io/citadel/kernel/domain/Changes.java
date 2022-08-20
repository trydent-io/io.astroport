package io.citadel.kernel.domain;

import io.citadel.kernel.eventstore.Audit;
import io.citadel.kernel.eventstore.EventStore;
import io.citadel.kernel.lang.Arrayable;
import io.vertx.core.Future;

import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public interface Changes<EVENT> {
  static <EVENT, STATE extends Enum<STATE> & State<STATE, EVENT>> Changes<EVENT> local(EventStore eventStore, Audit.Entity entity, STATE current) {
    return new Local<>(eventStore, entity, current);
  }
  static <EVENT> Changes<EVENT> nothing() { return (Changes<EVENT>) Nothing.Companion;}
  default <DOMAIN_EVENT extends EVENT> Changes<EVENT> add(DOMAIN_EVENT event) {
    return this;
  }
  default Future<Changes<EVENT>> commit() {
    return Future.succeededFuture(this);
  }
  enum Nothing implements Changes<Object> {
    Companion;
  }

  final class Local<EVENT, STATE extends Enum<STATE> & State<STATE, EVENT>> implements Changes<EVENT>, Arrayable<EVENT> {
    private final EventStore eventStore;
    private final Audit.Entity entity;
    private final STATE state;
    private final EVENT[] events;

    Local(EventStore eventStore, Audit.Entity entity, STATE state, EVENT... events) {
      this.eventStore = eventStore;
      this.entity = entity;
      this.state = state;
      this.events = events;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <DOMAIN_EVENT extends EVENT> Changes<EVENT> add(DOMAIN_EVENT event) {
      if (!state.transitable(event)) throw new IllegalStateException("Can't transit state %s with event %s".formatted(state, event));

      return new Local<>(eventStore, entity, state.transit(event), append(events, event));
    }

    @Override
    public Future<Changes<EVENT>> commit() {
      return eventStore
        .store(Stream.of(events).map(event -> Audit.of(entity, Audit.Event.from(event))))
        .map(Changes.nothing());
    }
  }
}


