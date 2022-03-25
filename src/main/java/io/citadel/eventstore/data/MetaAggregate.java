package io.citadel.eventstore.data;

import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;

public record MetaAggregate(String id, String name, long version) {
  public static MetaAggregate from(JsonObject json) {
    return fromJson(json);
  }

  public static MetaAggregate from(Row row) {
    return fromRow(row);
  }

  public static MetaAggregate fromJson(JsonObject json) {
    return new MetaAggregate(
      json.getString("id"),
      json.getString("name"),
      json.getLong("version")
    );
  }

  public static MetaAggregate fromRow(Row row) {
    return new MetaAggregate(
      row.getString("aggregate_id"),
      row.getString("aggregate_name"),
      row.getLong("aggregate_version")
    );
  }
}
