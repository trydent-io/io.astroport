package io.citadel.shared.domain;

import io.citadel.shared.domain.attribute.Serial;
import io.citadel.shared.func.ThrowableFunction;
import io.citadel.shared.func.ThrowableTriFunction;

import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public sealed interface Domain {
  enum Namespace implements Domain {}

  interface State {}
  interface Command {}
  interface Event {}

  interface Aggregate<A extends Aggregate<A>> {
    boolean is()
  }

  interface Attribute<T> extends Supplier<T> {

    default T get() {return value();}

    T value();
  }

  interface Version extends LongAttribute {
    static Domain.Version zero() {return new Serial(0);}
    static Optional<Domain.Version> of(long value) {
      return Optional.of(value).filter(it -> it >= 0).map(Serial::new);
    }
  }
  interface ID<T> extends Attribute<T> {}

  interface IntAttribute extends IntSupplier {
    default int getAsInt() {return value();}
    int value();
  }

  interface DoubleAttribute extends DoubleSupplier {
    default double getAsDouble() {return value();}
    double value();
  }

  interface BooleanAttribute extends BooleanSupplier {
    default boolean getAsBoolean() {return value();}
    boolean value();
  }

  interface LongAttribute extends LongSupplier {
    default long getAsLong() {return value();}
    long value();
  }
}

