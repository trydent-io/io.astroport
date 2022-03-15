package io.citadel.shared.context.attribute;

import java.util.function.BooleanSupplier;

public interface BooleanAttribute extends BooleanSupplier {
  default boolean getAsBoolean() {return value();}

  boolean value();

}
