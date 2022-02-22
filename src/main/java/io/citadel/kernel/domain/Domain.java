package io.citadel.kernel.domain;

import io.citadel.kernel.eventstore.EventLog;
import io.citadel.kernel.media.ArrayIterator;
import io.vertx.core.json.JsonObject;
import org.w3c.dom.Attr;

import java.util.Iterator;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public sealed interface Domain {
  enum Namespace implements Domain {}

  interface Command<E extends Event<?>> {
    E asEvent();
  }

  interface Event<C extends Command<?>> {
    C asCommand();

    default EventLog asEventLog(UUID aggregateId, String aggregateName, UUID revision) {
      return EventLog.of(this.getClass().getSimpleName(), JsonObject.mapFrom(this), aggregateId, aggregateName, revision);
    }
  }

  interface Aggregate<C extends Domain.Command<E>, E extends Domain.Event<C>, A extends Aggregate<C, E, A>> extends Iterable<E>, Function<C, A> {
    @Override
    default Iterator<E> iterator() { return new ArrayIterator<>(); }

    @SuppressWarnings("unchecked")
    default A flush() { return (A) this; }

    default A throwCommandException(C command) {
      throw new CommandException(command, this.getClass().getInterfaces()[0].getSimpleName(), getClass().getSimpleName());
    }
  }

  record Version(long getAsLong) implements LongSupplier {}

  interface Attribute<T> extends Supplier<T> {
    default T get() { return value(); }
    T value();
  }
  interface ID<T> extends Attribute<T> {}
  interface IntAttribute extends IntSupplier {
    default int getAsInt() { return value(); }
    int value();
  }
  interface DoubleAttribute extends DoubleSupplier {
    default double getAsDouble() { return value(); }
    double value();
  }
  interface BooleanAttribute extends BooleanSupplier {
    default boolean getAsBoolean() { return value(); }
    boolean value();

  }
  interface LongAttribute extends LongSupplier {
    default long getAsLong() { return value(); }
    long value();
  }
}
