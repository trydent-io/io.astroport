package io.citadel.kernel.eventstore;

import io.citadel.kernel.domain.Descriptor;
import io.citadel.kernel.domain.State;
import io.citadel.kernel.eventstore.meta.*;
import io.citadel.kernel.eventstore.meta.Feed;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.sqlclient.SqlClient;

public sealed interface Metadata permits Aggregates, Sql {
  static <ID, R, E, S extends Enum<S> & State<S, E>> Metadata<Context<R, E>> aggregate(Vertx vertx, SqlClient sqlClient, Descriptor<ID, R, E, S> descriptor) {
    return new Aggregates<>(vertx, descriptor, new Sql(sqlClient));
  }

  static Metadata<Feed> create(SqlClient client) {
    return new Sql(client);
  }

  default <T> Future<R> lookup(T entityId, String entityName, long entityVersion) {
    return lookup(Entity.id(entityId), Entity.name(entityName), Entity.version(entityVersion));
  }

  Future<R> lookup(ID id, Name name, Version version);
}
