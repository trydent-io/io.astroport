package io.citadel.kernel.eventstore;

import io.citadel.kernel.eventstore.meta.*;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;

import static io.citadel.kernel.eventstore.meta.Aggregate.*;
import static io.citadel.kernel.eventstore.meta.Aggregate.id;
import static io.citadel.kernel.eventstore.meta.Aggregate.name;

public sealed interface EventStore permits Lookup {
  static EventStore lookup(SqlClient client) {
    return new Lookup(client);
  }

  default <T> Future<Aggregate> aggregate(T id, String name) {
    return aggregate(id(id), name(name));
  }
  default Future<Aggregate> aggregate(ID id, Name name) {
    return aggregate(id, name, Version.Last);
  }
  default <T> Future<Aggregate> aggregate(T id, String name, long version) {
    return aggregate(id(id), name(name), version(version));
  }

  Future<Aggregate> aggregate(ID id, Name name, Version version);

}
