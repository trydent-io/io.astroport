package io.citadel.kernel.domain;

import io.citadel.kernel.domain.attribute.Attribute;
import io.citadel.kernel.domain.model.Defaults;
import io.citadel.kernel.domain.model.Service;
import io.citadel.kernel.eventstore.meta.ID;
import io.citadel.kernel.eventstore.meta.Timepoint;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface Domain {
  Defaults defaults = Defaults.Companion;

  sealed interface Verticle extends Domain, io.vertx.core.Verticle permits Service {}
  interface Migration extends Domain {
    Future<Void> migrate();
  }

  interface Aggregate<M extends Record, E, A extends Aggregate<M, E, A>> {
    default <T> Future<A> load(ID<T> id) {
      return load(id, -1);
    }
    Future<A> load(ID id, long version);
  }

  interface Model {
    <E> Model deserialize(BiFunction<? super String, ? super JsonObject, ? extends E> deserializer);

    <R extends Record> Model initialize(Function initializer);
  }
}

