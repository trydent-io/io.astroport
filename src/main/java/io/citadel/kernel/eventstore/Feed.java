package io.citadel.kernel.eventstore;

import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.eventstore.feed.Sql;
import io.citadel.kernel.eventstore.meta.Meta;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlClient;

import java.util.stream.Stream;

public sealed interface Feed permits Sql {
  static Feed create(Vertx vertx, SqlClient client) {
    return new Sql(vertx.eventBus(), client);
  }

  <ID extends Domain.ID<?>> Future<Meta> log(ID aggregateId, String aggregateName, long aggregateVersion, Stream<Event> events, String by);

  record Event(String name, JsonObject json) {}
}
