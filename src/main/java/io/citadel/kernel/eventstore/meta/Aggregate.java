package io.citadel.kernel.eventstore.meta;

import io.vertx.core.json.JsonObject;

public sealed interface Aggregate {
  static Aggregate entity(ID id, Version version, State state, Data data) {
    return new Entity(id, version, state, data);
  }

  static Aggregate identity(ID id) {
    return new Identity(id, Version.Zero);
  }

  static <T> ID id(T value) { return ID.of(value.toString()); }
  static Name name(String value) { return Name.of(value); }
  static Version version(long value) { return Version.of(value); }
  static State state(String value) { return State.of(value); }
  static Data data(JsonObject json) { return Data.of(json); }

  record Entity(ID id, Version version, State state, Data data) implements Aggregate {}
  record Identity(ID id, Version version) implements Aggregate {}
}
