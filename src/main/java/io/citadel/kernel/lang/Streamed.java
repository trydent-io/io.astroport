package io.citadel.kernel.lang;

import java.util.stream.Stream;

public interface Streamed<T> {
  @SuppressWarnings("unchecked")
  default Stream<T> append(Stream<T> origin, T... items) {
    return Stream.concat(origin, Stream.of(items));
  }
}
