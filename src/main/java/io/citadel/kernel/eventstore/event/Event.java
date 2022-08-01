package io.citadel.kernel.eventstore.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;

import java.time.LocalDateTime;
import java.util.UUID;

public record Event(ID id, Name name, Data data, Timepoint timepoint) {
  public Event(Name name, Data data) { this(null, name, data, null); }
  static Event uncommitted(String name, JsonObject data) {
    return new Event(null, name(name), data(data), null);
  }
  @JsonCreator
  public static Event committed(UUID id, String name, JsonObject data, LocalDateTime timepoint) {
    return new Event(id(id), name(name), data(data), timepoint(timepoint));
  }

  public static Event fromRow(Row row) {
    return new Event(
      id(row.getUUID("event_id")),
      name(row.getString("event_name")),
      data(row.getJsonObject("event_data")),
      timepoint(row.getLocalDateTime("event_timepoint"))
    );
  }

  public static ID id(UUID value) { return new ID(value); }
  public static Name name(String value) {
    return new Name(value);
  }
  public static Data data(JsonObject value) {
    return new Data(value);
  }
  public static Timepoint timepoint(LocalDateTime value) { return new Timepoint(value); }

  public static Event of(Name name, Data data) {
    return new Event(name, data);
  }

  public record ID(UUID value) { public ID { assert value != null; } }
  public record Name(String value) { public Name { assert value != null; } }
  public record Data(JsonObject value) { public Data { assert value != null; } }
  public record Timepoint(LocalDateTime value) { public Timepoint { assert value != null; } }
}
