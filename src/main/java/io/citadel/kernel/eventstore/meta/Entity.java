package io.citadel.kernel.eventstore.meta;

import io.citadel.kernel.domain.Descriptor;
import io.citadel.kernel.domain.Domain.Aggregate;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlClient;

public sealed interface Entity {

  static <T> Entity of(T id, String name, long version) {
    return new Entity(id(id), name(name), version(version));
  }

  static Entity of(ID id, Name name, Version version) {
    return new Entity(id, name, version);
  }

  static <T> ID id(T value) { return ID.of(value.toString()); }
  static Name name(String value) {
    return Name.of(value);
  }
  static Version version(Long value) { return Version.of(value); }
  static State state(String value) { return State.of(value); }
  static Data data(JsonObject value) {
    return Data.of(value);
  }

  static Entity found(SqlClient client, ID id, Name name, Version version, State state, Data data) {
    return new Found(client, id, name, version, state, data);
  }

  <I, R extends Record, E, S extends Enum<S> & io.citadel.kernel.domain.State<S, E>> Aggregate<R, E> aggregate(Descriptor<I, R, E, S> descriptor);
}

final class Found implements Entity {
  private final SqlClient client;
  private final ID id;
  private final Name name;
  private final Version version;
  private final State state;
  private final Data data;

  public Found(SqlClient client, ID id, Name name, Version version, State state, Data data) {
    this.client = client;
    this.id = id;
    this.name = name;
    this.version = version;
    this.state = state;
    this.data = data;
  }

  @Override
  public <I, R extends Record, E, S extends Enum<S> & io.citadel.kernel.domain.State<S, E>> Aggregate<R, E> aggregate(Descriptor<I, R, E, S> descriptor) {
    final var entity = descriptor.entity(id.as(descriptor::id));
    final var current = state.as(descriptor::state);
    data.with(json -> json.put("id", id.as(descriptor::id)));
    return null;
  }
}
