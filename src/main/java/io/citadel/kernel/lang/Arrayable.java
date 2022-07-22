package io.citadel.kernel.lang;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

public interface Arrayable<T> {
  @SuppressWarnings("unchecked")
  default T[] append(T[] elements, T... newElements) {
    var copied = Arrays.copyOf(elements, elements.length + newElements.length);
    System.arraycopy(elements, 0, copied, elements.length, newElements.length);
    return copied;
  }
}
