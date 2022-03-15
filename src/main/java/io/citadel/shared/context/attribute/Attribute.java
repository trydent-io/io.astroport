package io.citadel.shared.context.attribute;

import java.util.function.Supplier;

public interface Attribute<T> extends Supplier<T> {

  default T get() {return value();}

  T value();
}
