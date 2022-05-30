package io.citadel.kernel.eventstore;

import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.eventstore.lookup.Sql;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;


public sealed interface Lookup permits io.citadel.kernel.eventstore.lookup.Sql {
  static Lookup sql(SqlClient client) {
    return new Sql(client);
  }

  <ID extends Domain.ID<?>> Future<Prototype<ID>> findPrototype(ID aggregateId, String aggregateName, long aggregateVersion);
  default <ID extends Domain.ID<?>> Future<Prototype<ID>> findPrototype(ID aggregateId, String aggregateName) {
    return findPrototype(aggregateId, aggregateName, -1);
  }
  default <ID extends Domain.ID<?>> Future<Prototype<ID>> findPrototype(ID aggregateId) {
    return findPrototype(aggregateId, null);
  }
}
