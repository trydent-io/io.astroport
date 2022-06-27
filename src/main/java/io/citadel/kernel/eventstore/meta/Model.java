package io.citadel.kernel.eventstore.meta;

import io.citadel.kernel.func.ThrowableFunction;
import io.vertx.core.json.JsonObject;

import java.util.function.Consumer;

public record Model(JsonObject value) {
  public Model { assert value != null; }

  static Model of(JsonObject value) {
    return switch (value) {
      case null -> throw new IllegalArgumentException("Data can't be null");
      default -> new Model(value);
    };
  }

  public <R extends Record> R as(ThrowableFunction<? super JsonObject, ? extends R> converter) {
    return converter.apply(value);
  }

  public Model with(Consumer<? super JsonObject> wither) {
    wither.accept(value);
    return this;
  }
}
