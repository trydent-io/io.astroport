package io.citadel.kernel.domain.attribute;

import java.util.function.IntSupplier;

public interface IntAttribute extends IntSupplier {
  default int getAsInt() {return value();}

  int value();

}
