package io.citadel.kernel.domain;

import io.citadel.kernel.eventstore.EventLog;
import io.citadel.kernel.func.ThrowableFunction;
import io.citadel.kernel.func.ThrowableTriFunction;
import io.citadel.kernel.media.ArrayIterator;
import io.vertx.core.json.JsonObject;

import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public sealed interface Domain {
  enum Namespace implements Domain {}

  interface Command {}

  interface Event {
    default EventLog asEventLog(UUID aggregateId, String aggregateName, UUID revision) {
      return EventLog.of(this.getClass().getSimpleName(), JsonObject.mapFrom(this), aggregateId, aggregateName, revision);
    }
  }

  interface Aggregate<A extends Aggregate<A>> extends ThrowableFunction<ThrowableTriFunction<Domain.ID<?>, Domain.Version, Domain.Event[], A>, A> {}

  interface Attribute<T> extends Supplier<T> {

    default T get() {return value();}

    T value();
  }

  record Version(long value) implements LongAttribute {
    public Version {
      if (value < 0)
        throw new IllegalArgumentException("Version can't be less than 0");
    }

    public static Version zero() {return new Version(0);}
    public static Optional<Version> of(long value) {
      return Optional.of(value).filter(it -> it >= 0).map(Version::new);
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
