package io.citadel.kernel.media;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

public sealed interface Array<T> extends Iterable<T> {
  Iterators iterators = Iterators.Defaults;

  @SafeVarargs
  static <T> Array<T> of(T... elements) {
    return new Type.Readonly<>(elements);
  }

  @SuppressWarnings("unchecked")
  static <T> Array<T> empty() {
    return (Array<T>) Type.Empty.Default;
  }

  default Array<T> push(T... elements) {
    return switch (this) {
      case Type.Empty ignored -> io.citadel.kernel.media.Array.of(elements);
      case Type.Readonly<T> readonly -> {
        var length = readonly.elements.length + elements.length;
        var copied = Arrays.copyOf(readonly.elements, length);
        System.arraycopy(elements, 0, copied, readonly.elements.length, elements.length);
        yield new Type.Readonly<>(copied);
      }
    };
  }

  default T at(int index) {
    return switch (this) {
      case Type.Empty ignored -> null;
      case Type.Readonly<T> readonly -> readonly.elements.length > index ? readonly.elements[index] : null;
    };
  }

  default Stream<T> stream() {
    return switch (this) {
      case Type.Empty ignored -> Stream.empty();
      case Type.Readonly<T> readonly -> Stream.of(readonly.elements);
    };
  }

  @Override
  default Iterator<T> iterator() {
    return switch (this) {
      case Type.Empty ignored -> Array.iterators.empty();
      case Type.Readonly<T> readonly -> Array.iterators.iterator(readonly.elements);
    };
  }

  enum Type {;
    private enum Empty implements Array<Object> { Default }
    private record Readonly<T>(T[] elements) implements Array<T> {}
  }
}

