package io.citadel.kernel.domain.attribute;

import java.util.function.LongSupplier;

public interface LongAttribute extends LongSupplier {
  default long getAsLong() {return value();}

  long value();

}
