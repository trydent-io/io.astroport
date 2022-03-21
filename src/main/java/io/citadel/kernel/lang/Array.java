package io.citadel.kernel.lang;

import io.vertx.core.json.JsonArray;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public sealed interface Array<T> extends Iterable<T> {
  Iterators iterators = Iterators.Defaults;

  @SafeVarargs
  static <T> Array<T> of(T... elements) {
    return new Type.Pushed<>(Arrays.copyOf(elements, elements.length));
  }

  @SuppressWarnings("unchecked")
  static <T> Array<T> empty() {
    return (Array<T>) Type.Empty.Default;
  }

  default Optional<T> last() {
    return switch (this) {
      case Type.Empty ignored -> Optional.empty();
      case Type.Some<T> some -> Optional.ofNullable(some.elements().length > 0 ? some.elements()[some.elements().length - 1] : null);
    };
  }

  default Optional<T> first() {
    return switch (this) {
      case Type.Empty ignored -> Optional.empty();
      case Type.Some<T> some -> Optional.ofNullable(some.elements().length > 0 ? some.elements()[0] : null);
    };
  }

  default Array<T> push(T... elements) {
    return switch (this) {
      case Type.Empty ignored -> Array.of(elements);
      case Type.Some<T> some -> {
        final var someElements = some.elements();
        var copied = Arrays.copyOf(someElements, someElements.length + elements.length);
        System.arraycopy(elements, 0, copied, someElements.length, elements.length);
        yield new Type.Pushed<>(copied);
      }
    };
  }

  default Array<T> pop() {
    return switch (this) {
      case Type.Empty ignored -> this;
      case Type.Some<T> some -> {
        final var someElements = some.elements();
        var copied = Arrays.copyOf(someElements, someElements.length - 1);
        System.arraycopy(someElements, 0, copied, someElements.length - 1, copied.length);
        yield new Type.Popped<>(copied, someElements[someElements.length - 1]);
      }
    };
  }

  default Optional<T> out() {
    return switch (this) {
      case Type.Popped<T> popped -> Optional.ofNullable(popped.outer);
      default -> Optional.empty();
    };
  }

  default Optional<T> at(int index) {
    return switch (this) {
      case Type.Empty ignored -> Optional.empty();
      case Type.Some<T> some -> Optional.ofNullable(index >= 0 && some.elements().length > index ? some.elements()[index] : null);
    };
  }

  default <R> Array<R> map(IntFunction<R[]> fromIndex, Function<? super T, ? extends R> asR) {
    return switch (this) {
      case Type.Empty ignored -> Array.empty();
      case Type.Some<T> some -> Array.of(some.stream().map(asR).toArray(fromIndex));
    };
  }

  default JsonArray asJsonArray() {
    return switch (this) {
      case Type.Empty ignored -> new JsonArray();
      case Type.Some<T> some -> new JsonArray(List.of(some.elements()));
    };
  }

  default Stream<T> stream() {
    return switch (this) {
      case Type.Empty ignored -> Stream.empty();
      case Type.Some<T> some -> StreamSupport.stream(spliterator(), false);
    };
  }

  @Override
  default Spliterator<T> spliterator() {
    return switch (this) {
      case Type.Empty ignored -> Spliterators.emptySpliterator();
      case Type.Some<T> some -> Arrays.spliterator(some.elements());
    };
  }

  @Override
  default Iterator<T> iterator() {
    return switch (this) {
      case Type.Empty ignored -> Array.iterators.empty();
      case Type.Some<T> some -> Array.iterators.iterator(some.elements());
    };
  }

  enum Type {;

    private sealed interface Some<T> extends Array<T> { T[] elements(); }

    private enum Empty implements Array<Object> {Default}

    private record Pushed<T>(T[] elements) implements Some<T> {}
    private record Popped<T>(T[] elements, T outer) implements Some<T> {}
  }

  static void main(String[] args) {
    final var out = Array.of(1, 2, 3).pop().push(1, 2, 3);
  }
}

