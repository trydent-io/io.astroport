package io.citadel.kernel.domain.attribute;

import java.util.function.DoubleSupplier;

public interface DoubleAttribute extends DoubleSupplier {
  default double getAsDouble() {return value();}

  double value();

}
