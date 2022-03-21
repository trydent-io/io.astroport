package io.citadel.kernel.domain.attribute;

import java.util.function.Supplier;

public interface Attribute<T> extends Supplier<T> {

  default T get() {return value();}

  T value();
}
