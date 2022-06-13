package io.citadel.kernel.eventstore.meta;

import io.vertx.core.json.JsonObject;

public record Event(String name, JsonObject data) {

  public static Name name(String value) {
    return switch (value) {
      case null -> throw new IllegalArgumentException("Name can't be null");
      case String it && (it.isEmpty() || it.isBlank()) -> throw new IllegalArgumentException("Name can't be empty or blank");
      default -> new Name(value);
    };
  }

}
