package io.citadel.kernel.eventstore.metadata;

import io.vertx.core.json.JsonObject;

public sealed interface Aggregate {
  static Aggregate last(ID id, Name name, Version version, State state, Model model) {
    return new Last(id, name, version, state, model);
  }

  static Aggregate zero(ID id, Name name) {
    return new Zero(id, name, Version.Zero);
  }

  static <T, M extends Record, E, S extends Enum<S> & io.citadel.kernel.domain.State<S, E>> Aggregate root(T id, M model, S state, Version version) {
    return new Root<>(id, model, state, version);
  }

  static <T> ID id(T value) { return ID.of(value.toString()); }
  static Name name(String value) { return Name.of(value); }
  static Version version(long value) { return Version.of(value); }
  static State state(String value) { return State.of(value); }
  static Model model(JsonObject json) { return Model.of(json); }

  record Last(ID id, Name name, Version version, State state, Model entity) implements Aggregate {}
  record Zero(ID id, Name name, Version version) implements Aggregate {}
  record Root<T, M extends Record, E, S extends Enum<S> & io.citadel.kernel.domain.State<S, E>>(T id, M model, S state, Version version) implements Aggregate {}
}
