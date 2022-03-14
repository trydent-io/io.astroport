package io.citadel.shared.context;

import io.citadel.shared.context.attribute.Serial;
import io.citadel.shared.func.Maybe;
import io.citadel.shared.func.ThrowableBiFunction;
import io.vertx.core.Future;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static io.citadel.eventstore.EventStore.EventInfo;
import static java.lang.Long.MAX_VALUE;

public sealed interface Domain {
  enum Namespace implements Domain {}

  interface State<E extends Enum<E>> {}
  interface Command {}
  interface Event {}
  interface Model {}

  interface Aggregate<S extends State<?>> {
    boolean is(S state);
  }
  interface Repository<A extends Aggregate<?>, I extends ID<?>, M extends Model> {
    Future<A> load(I id);
  }

  interface Hydration<A extends Aggregate<?>> extends ThrowableBiFunction<Version, Stream<EventInfo>, A> {}

  interface Attribute<T> extends Supplier<T> {

    default T get() {return value();}

    T value();
  }

  interface Version extends LongAttribute {
    static Domain.Version first() {return Versions.Defaults.Zero;}
    static Domain.Version last() {return Versions.Defaults.Last;}
    static Maybe<Version> of(long value) {
      return Maybe.of(value).filter(it -> it >= 0).map(Serial::new);
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

enum Versions {;
  enum Defaults implements Domain.Version {
    Zero {
      @Override
      public long value() {
        return 0;
      }
    },
    Last {
      @Override
      public long value() {
        return MAX_VALUE;
      }
    }
  }
}
