package io.citadel.kernel.media;

import java.util.Iterator;

public final class ArrayIterator<T> implements Iterator<T>, Iterable<T> {
  private final T[] array;
  private int index;

  @SafeVarargs
  public ArrayIterator(T... a) {
    this.array = a;
    this.index = 0;
  }

  public boolean hasNext() {
    return this.index < this.array.length;
  }

  public T next() {
    return this.index >= this.array.length
      ? this.array[this.array.length - 1]
      : this.array[this.index++];
  }

  public void remove() {}

  public Iterator<T> iterator() {
    return this;
  }
}
