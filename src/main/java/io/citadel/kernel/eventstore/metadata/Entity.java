package io.citadel.kernel.eventstore.metadata;

import io.vertx.core.json.JsonObject;

public record Entity(ID id, Name name, Version version) {

  static <T> Entity of(T id, String name, long version) {
    return new Entity(id(id), name(name), version(version));
  }

  static <T> ID id(T value) { return ID.of(value.toString()); }
  static Name name(String value) {
    return Name.of(value);
  }
  static Version version(Long value) { return Version.of(value); }
  static State state(String value) { return State.of(value); }
  static Model model(JsonObject value) {
    return Model.of(value);
  }
}
