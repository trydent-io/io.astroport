package io.citadel.kernel.domain;

import io.citadel.eventstore.data.AggregateInfo;
import io.citadel.eventstore.data.EventInfo;

import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public interface Eventable<E extends Domain.Event> {
  default Stream<E> append(Stream<E> origin, E... events) {
    return Stream.concat(origin, Stream.of(events));
  }

  default AggregateInfo aggregate(String id, String name, long version) {
    return new AggregateInfo(id, name, version);
  }

  default Stream<EventInfo> events(Stream<E> events) {
    return events.map(Domain.Event::asInfo);
  }
}
