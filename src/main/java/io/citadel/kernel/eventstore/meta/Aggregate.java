package io.citadel.kernel.eventstore.meta;

import io.vertx.core.json.JsonObject;

public sealed interface Aggregate {
  static Aggregate entity(ID id, Name name, Version version, State state, Model model) {
    return new Entity(id, name, version, state, model);
  }

  static Aggregate identity(ID id, Name name) {
    return new Identity(id, name, Version.Zero);
  }

  static <T> ID id(T value) { return ID.of(value.toString()); }
  static Name name(String value) { return Name.of(value); }
  static Version version(long value) { return Version.of(value); }
  static State state(String value) { return State.of(value); }
  static Model model(JsonObject json) { return Model.of(json); }

  record Entity(ID id, Name name, Version version, State state, Model model) implements Aggregate {}
  record Identity(ID id, Name name, Version version) implements Aggregate {}
}
