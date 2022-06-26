package io.citadel.kernel.eventstore.meta;

import io.citadel.kernel.domain.Descriptor;
import io.citadel.kernel.domain.Domain.Aggregate;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlClient;

public record Entity(ID id, Name name, Version version) {

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

}
