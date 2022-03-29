package io.citadel.kernel.domain;

import java.util.stream.Stream;

public interface Eventable<E extends Domain.Event> {
  default Stream<E> append(Stream<E> origin, E... events) {
    return Stream.concat(origin, Stream.of(events));
  }
}
