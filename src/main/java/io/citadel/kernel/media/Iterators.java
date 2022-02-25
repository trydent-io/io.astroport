package io.citadel.kernel.media;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

public enum Iterators {
  Defaults;

  @SafeVarargs
  public final <T> Iterator<T> iterator(T... elements) {
    return Array.iterator(elements);
  }

  public <T> Iterator<T> empty() {
    return Array.empty();
  }

  public sealed interface Array<T> extends Iterator<T> {
    @SafeVarargs
    static <T> Array<T> iterator(T... elements) {
      return new Type.Iterator<>(Arrays.copyOf(elements, elements.length));
    }

    @SuppressWarnings("unchecked")
    static <T> Array<T> empty() {
      return (Array<T>) Type.Empty.Default;
    }


    default boolean hasNext() {
      return switch (this) {
        case Type.Empty ignored -> false;
        case Type.Iterator<T> iterator -> iterator.index.get() < iterator.elements.length;
      };
    }

    default T next() {
      return switch (this) {
        case Type.Empty ignored -> null;
        case Type.Iterator<T> iterator -> iterator.index.get() >= iterator.elements.length
          ? iterator.elements[iterator.elements.length - 1]
          : iterator.elements[iterator.index.getAndIncrement()];
      };
    }

    enum Type {;
      private enum Empty implements Iterators.Array<Object> {Default}
      private record Iterator<T>(T[] elements, AtomicInteger index) implements Iterators.Array<T> {
        Iterator(T[] elements) {this(elements, new AtomicInteger(0));}
      }
    }
  }
}
