package io.citadel.kernel.eventstore.metadata;

import io.citadel.kernel.eventstore.audit.ID;
import io.citadel.kernel.eventstore.audit.Name;
import io.citadel.kernel.eventstore.audit.Version;
import io.vertx.core.json.JsonObject;

import java.util.function.Function;

public sealed interface MetaAggregate {
  static MetaAggregate last(ID id, Name name, Version version, State state, Model model) {
    return new Last(id, name, version, state, model);
  }

  static MetaAggregate zero(ID id, Name name) {
    return new Zero(id, name, Version.Zero);
  }

  static <T, M extends Record, E, S extends Enum<S> & io.citadel.kernel.domain.State<S, E>> MetaAggregate root(T id, M model, S state, Version version) {
    return new Root<>(id, model, state, version);
  }

  static <T> ID id(T value) { return ID.of(value.toString()); }
  static Name name(String value) { return Name.of(value); }
  static Version version(long value) { return Version.of(value); }
  static State state(String value) { return State.of(value); }
  static Model model(JsonObject json) { return Model.of(json); }

  record Last(ID id, Name name, Version version, State state, Model entity) implements MetaAggregate {
    public <T> T id(Function<? super String, ? extends T> transformer) {return transformer.apply(id.value());}
    public <T extends Record> T entity(Function<? super JsonObject, ? extends T> transformer) {return transformer.apply(entity.value());}
    public <T extends Enum<T>> T state(Function<? super String, ? extends T> transformer) {return transformer.apply(state.value());}
  }
  record Zero(ID id, Name name, Version version) implements MetaAggregate {
    public <T> T id(Function<? super String, ? extends T> transformer) {return transformer.apply(id.value());}
  }
  record Root<T, M extends Record, E, S extends Enum<S> & io.citadel.kernel.domain.State<S, E>>(T id, M model, S state, Version version) implements MetaAggregate {}
}
