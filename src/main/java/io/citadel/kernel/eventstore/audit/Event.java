package io.citadel.kernel.eventstore.audit;

import io.vertx.core.json.JsonObject;

public record Event(Name name, Data data) {
  public static Event of(String name, JsonObject data) {
    return new Event(name(name), data(data));
  }
  public static Name name(String value) {
    return Name.of(value);
  }
  public static Data data(JsonObject json) {
    return Data.of(json);
  }
}
