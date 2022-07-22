package io.citadel.kernel.domain;

import io.citadel.kernel.eventstore.event.Entity;
import io.citadel.kernel.eventstore.event.Event;
import io.citadel.kernel.eventstore.metadata.MetaAggregate;
import io.citadel.kernel.eventstore.event.Name;
import io.citadel.kernel.eventstore.event.Version;
import io.citadel.kernel.func.TryBiFunction;
import io.citadel.kernel.func.TrySupplier;

import java.util.function.Function;
import java.util.function.Supplier;

sealed public interface Aggregator<E, S extends Enum<S> & State<S, E>, A> {

  static <E, S extends Enum<S> & State<S, E>, A> Aggregator<E, S, A> model(String name) {
    return new Model<>(Entity.name(name), null, null, null);
  }

  Aggregator<E, S, A> state(S state);
  Aggregator<E, S, A> zero(TrySupplier<A> zero);
  Aggregator<E, S, A> last(TryBiFunction<? super A, ? super Event, ? extends A> last);

  default A versioned(long version) { return versioned(Entity.version(version)); }
  A versioned(Entity.Version version);

  record Model<E, S extends Enum<S> & State<S, E>, A>(Entity.Name name, S state, TrySupplier<A> zero, TryBiFunction<? super A, ? super Event, ? extends A> last) implements Aggregator<E, S, A> {
    @Override
    public Aggregator<E, S, A> state(S state) {
      return new Aggregator.Model<>(name, state, zero, last);
    }

    @Override
    public Aggregator<E, S, A> zero(TrySupplier<A> zero) {
      return new Aggregator.Model<>(name, state, zero, last);
    }

    @Override
    public Aggregator<E, S, A> last(TryBiFunction<? super A, ? super Event, ? extends A> last) {
      return new Aggregator.Model<>(name, state, zero, last);
    }

    @Override
    public A versioned(Entity.Version version) {
      return null;
    }

  }
}
