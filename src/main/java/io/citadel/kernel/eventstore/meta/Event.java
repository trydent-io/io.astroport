package io.citadel.kernel.eventstore.meta;

import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Event(Name name, Data data, Timepoint timepoint) {
  public static Event of(String name, JsonObject data, LocalDateTime timepoint) {
    return new Event(name(name), data(data), timepoint(timepoint));
  }
  public static Event fromRow(Row row) {
    return new Event(
      name(row.getString("event_name")),
      data(row.getJsonObject("event_data")),
      timepoint(row.getLocalDateTime("timepoint"))
    );
  }
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
