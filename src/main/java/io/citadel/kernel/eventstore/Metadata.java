package io.citadel.kernel.eventstore;

import io.citadel.kernel.eventstore.meta.Entity;
import io.citadel.kernel.eventstore.meta.ID;
import io.citadel.kernel.eventstore.meta.Name;
import io.citadel.kernel.eventstore.meta.Version;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;

public sealed interface Metadata permits Aggregates, Lookup {
  static Metadata lookup(SqlClient client) {
    return new Lookup(client);
  }

  default <T> Future<Entity> findEntity(T id, String name, long version) {
    return findEntity(Entity.id(id), Entity.name(name), Entity.version(version));
  }

  Future<Entity> findEntity(ID id, Name name, Version version);
}
