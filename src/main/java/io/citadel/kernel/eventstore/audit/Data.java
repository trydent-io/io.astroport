package io.citadel.kernel.eventstore.audit;

import io.citadel.kernel.func.ThrowableFunction;
import io.vertx.core.json.JsonObject;

import java.util.function.Consumer;

public record Data(JsonObject value) {
  public Data { assert value != null; }

  static Data of(JsonObject value) {
    return switch (value) {
      case null -> throw new IllegalArgumentException("Data can't be null");
      default -> new Data(value);
    };
  }

  public <R extends Record> R as(ThrowableFunction<? super JsonObject, ? extends R> converter) {
    return converter.apply(value);
  }

  public Data with(Consumer<? super JsonObject> wither) {
    wither.accept(value);
    return this;
  }
}
