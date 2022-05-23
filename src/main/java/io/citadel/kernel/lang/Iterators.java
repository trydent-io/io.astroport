package io.citadel.kernel.lang;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public enum Iterators {
  defaults;

  @SafeVarargs
  public final <T> java.util.Iterator<T> iterator(T... elements) {
    return Iterator.some(elements);
  }

  public <T> java.util.Iterator<T> empty() {
    return Iterator.empty();
  }

  public sealed interface Iterator<T> extends java.util.Iterator<T> {
    @SafeVarargs
    static <T> Iterator<T> some(T... elements) {
      return new Type.Some<>(Arrays.copyOf(elements, elements.length));
    }

    @SuppressWarnings("unchecked")
    static <T> Iterator<T> empty() {
      return (Iterator<T>) Type.Empty.Default;
    }


    default boolean hasNext() {
      return switch (this) {
        case Type.Empty ignored -> false;
        case Type.Some<T> some -> some.index.get() < some.elements.length;
      };
    }

    default T next() {
      return switch (this) {
        case Type.Empty ignored -> null;
        case Type.Some<T> some -> some.index.get() >= some.elements.length
          ? some.elements[some.elements.length - 1]
          : some.elements[some.index.getAndIncrement()];
      };
    }

    enum Type {;
      private enum Empty implements Iterator<Object> {Default}
      private record Some<T>(T[] elements, AtomicInteger index) implements Iterator<T> {
        Some(T[] elements) {this(elements, new AtomicInteger(0));}
      }
    }
  }
}
