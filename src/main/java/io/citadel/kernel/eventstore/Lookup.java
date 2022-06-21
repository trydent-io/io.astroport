package io.citadel.kernel.eventstore;

import io.citadel.kernel.domain.Descriptor;
import io.citadel.kernel.domain.State;
import io.citadel.kernel.eventstore.meta.*;
import io.citadel.kernel.eventstore.meta.Feed;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.sqlclient.SqlClient;

public sealed interface Lookup<R> permits Aggregates, Sql {
  static <ID, R, E, S extends Enum<S> & State<S, E>> Lookup<Context<R, E>> aggregate(Vertx vertx, SqlClient sqlClient, Descriptor<ID, R, E, S> descriptor) {
    return new Aggregates<>(vertx, descriptor, new Sql(sqlClient));
  }

  static Lookup<Feed> create(SqlClient client) {
    return new Sql(client);
  }

  default <T> Future<R> find(T aggregateId, String aggregateName, long aggregateVersion) {
    return find(Entity.id(aggregateId), Entity.name(aggregateName), Entity.version(aggregateVersion));
  }

  Future<R> find(ID id, Name name, Version version);
}
