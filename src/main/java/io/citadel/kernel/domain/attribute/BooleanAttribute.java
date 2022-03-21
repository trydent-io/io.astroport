package io.citadel.kernel.domain.attribute;

import java.util.function.BooleanSupplier;

public interface BooleanAttribute extends BooleanSupplier {
  default boolean getAsBoolean() {return value();}

  boolean value();

}
