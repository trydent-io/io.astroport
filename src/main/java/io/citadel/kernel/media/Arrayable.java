package io.citadel.kernel.media;

import java.util.Arrays;

public interface Arrayable {
  default <T> T[] push(T[] array, T... elements) {
    final int size = array.length + elements.length;
    final T[] copied = Arrays.copyOf(array, size);
    System.arraycopy(elements, 0, copied, array.length, elements.length);
    return copied;
  }
}
