package io.citadel.shared.context.attribute;

import java.util.function.LongSupplier;

public interface LongAttribute extends LongSupplier {
  default long getAsLong() {return value();}

  long value();

}
