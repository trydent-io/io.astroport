package io.citadel.shared.media;

import java.util.stream.Stream;

public interface Streamed {
  @SuppressWarnings("unchecked")
  default <T> T[] concat(T[] destination, T... elements) {
    return (T[]) concat(Stream.of(destination), Stream.of(elements)).toArray();
  }

  default <T> Stream<T> concat(Stream<? extends T> destination, Stream<? extends T> elements) {
    return Stream.concat(destination, elements);
  }
}
