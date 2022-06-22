package io.citadel.kernel.eventstore.meta;

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

  public <R extends Record> R as(Class<R> type) {
    return value.mapTo(type);
  }

  public Data with(Consumer<? super JsonObject> wither) {
    wither.accept(value);
    return this;
  }
}
