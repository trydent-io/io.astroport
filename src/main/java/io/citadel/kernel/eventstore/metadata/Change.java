package io.citadel.kernel.eventstore.metadata;

import io.citadel.kernel.eventstore.audit.Data;
import io.citadel.kernel.eventstore.audit.ID;
import io.citadel.kernel.eventstore.audit.Name;
import io.citadel.kernel.eventstore.audit.Timepoint;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;

import java.time.LocalDateTime;

public record Change(ID aggregateId, Name eventName, Data eventData, Timepoint timepoint) {
  public static Change of(String aggregateId, String eventName, JsonObject eventData, LocalDateTime timepoint) {
    return new Change(id(aggregateId), name(eventName), data(eventData), timepoint(timepoint));
  }
  public static Change fromRow(Row row) {
    return new Change(
      id(row.getString("aggregate_id")),
      name(row.getString("event_name")),
      data(row.getJsonObject("event_data")),
      timepoint(row.getLocalDateTime("timepoint"))
    );
  }
  public static ID id(String value) { return ID.of(value); }
  public static Name name(String value) {
    return Name.of(value);
  }
  public static Data data(JsonObject json) {
    return Data.of(json);
  }

  public static Timepoint timepoint(LocalDateTime value) {
    return Timepoint.of(value);
  }
}
