package io.citadel.kernel.domain;

import io.citadel.kernel.eventstore.Meta;
import io.citadel.kernel.domain.attribute.Attribute;
import io.citadel.kernel.domain.model.Defaults;
import io.citadel.kernel.domain.model.Service;
import io.citadel.kernel.func.ThrowableBiFunction;
import io.citadel.kernel.func.ThrowableFunction;
import io.citadel.kernel.func.ThrowablePredicate;
import io.citadel.kernel.func.ThrowableSupplier;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

public sealed interface Domain {
  Defaults defaults = Defaults.Companion;

  sealed interface Verticle extends Domain, io.vertx.core.Verticle permits Service {}
  non-sealed interface Migration extends Domain {
    Future<Void> migrate();
  }

  interface State<S extends Enum<S> & State<S, E>, E extends Domain.Event> {
    @SuppressWarnings("unchecked")
    default boolean is(S... states) {
      var index = 0;
      while (index < states.length && states[index] != this)
        index++;
      return index < states.length;
    }
    Optional<S> next(E event);
  }

  interface Command {}

  interface Event {
    default Meta.Event asFeed() {return new Meta.Event(this.getClass().getSimpleName(), JsonObject.mapFrom(this), new Meta.Timepoint(LocalDateTime.now()));}
  }

  interface Model<ID extends Domain.ID<?>> {
    ID id();
  }

  interface ID<T> extends Attribute<T> {}
}

