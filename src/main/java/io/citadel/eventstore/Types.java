package io.citadel.eventstore;

import io.citadel.eventstore.EventStore.EventLog;
import io.citadel.shared.func.Maybe;
import io.vertx.sqlclient.Row;

import static io.citadel.eventstore.EventStore.*;

public enum Types {
  Defaults;

  public Raw aggregate(Row row) {
    return new Raw(
      row.getString("aggregate_id"),
      row.getString("aggregate_name"),
      row.getLong("aggregate_version")
    );
  }

  public EventInfo event(Row row) {
    return new EventInfo(
      row.getString("event_name"),
      row.getJsonObject("event_data")
    );
  }

  public EventLog eventLog(Row row) {
    return new EventLog(
      row.getUUID("id"),
      aggregate(row),
      event(row),
      row.getLocalDateTime("persisted_at"),
      Maybe.of(row.getString("persisted_by")).or("none")
    );
  }
}
