package io.citadel.kernel.media;

import java.util.Arrays;

public interface Array<T> {
  @SafeVarargs
  static <T> Array<T> of(T... elements) {
    return new Elements<>(elements);
  }
  @SuppressWarnings("unchecked")
  static <T> Array<T> empty() {
    return (Array<T>) Empty.Default;
  }

  T[] elements();

  default Array<T> push(T... elements) {
    final int length = elements().length + elements.length;
    final T[] copied = Arrays.copyOf(elements(), length);
    System.arraycopy(elements, 0, copied, elements().length, elements.length);
    return new Elements<>(copied);
  }

  default T at(int index) { return elements().length > index ? elements()[index] : null; }
}

enum Empty implements Array<Object> { Default;
  @Override
  public Object[] elements() {
    return new Object[0];
  }
}
record Elements<T>(T[] elements) implements Array<T> {}
