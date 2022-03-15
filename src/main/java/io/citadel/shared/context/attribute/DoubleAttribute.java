package io.citadel.shared.context.attribute;

import java.util.function.DoubleSupplier;

public interface DoubleAttribute extends DoubleSupplier {
  default double getAsDouble() {return value();}

  double value();

}
