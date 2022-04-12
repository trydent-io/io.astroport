package io.citadel.kernel.domain.eventstore.data;

import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;

public record AggregateInfo(String id, String name, long version) {
  public static AggregateInfo from(JsonObject json) {
    return fromJson(json);
  }

  public static AggregateInfo from(Row row) {
    return fromRow(row);
  }

  public static AggregateInfo fromJson(JsonObject json) {
    return new AggregateInfo(
      json.getString("id"),
      json.getString("name"),
      json.getLong("version")
    );
  }

  public static AggregateInfo fromRow(Row row) {
    return new AggregateInfo(
      row.getString("aggregate_id"),
      row.getString("aggregate_name"),
      row.getLong("aggregate_version")
    );
  }
}
