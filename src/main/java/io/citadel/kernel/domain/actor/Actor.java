package io.citadel.kernel.domain.actor;

import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.func.ThrowableBiFunction;
import io.citadel.kernel.func.ThrowableFunction;
import io.vertx.core.json.JsonObject;

public interface Actor<ID extends Domain.ID<?>, M extends Record & Domain.Model<ID>, E extends Domain.Event, S extends Enum<S> & Domain.State<S, E>> {
  ThrowableFunction<? super ID, ? extends M> identity();
  ThrowableBiFunction<? super String, ? super JsonObject, ? extends E> normalize();
  ThrowableBiFunction<? super M, ? super E, ? extends M> hydrate();
  S initial();
}
