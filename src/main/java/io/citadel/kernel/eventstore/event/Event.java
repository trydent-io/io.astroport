package io.citadel.kernel.eventstore.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.vertx.core.json.JsonObject;

import java.time.LocalDateTime;
import java.util.UUID;

public sealed interface Event {
  record Unsaved(Name name, Data data) implements Event {}
  record Saved(ID id, Name name, Data data, Timepoint timepoint) implements Event {}

  static Event unsaved(String name, JsonObject data) {
    return new Unsaved(name(name), data(data));
  }
  @JsonCreator
  static Event saved(UUID id, String name, JsonObject data, LocalDateTime timepoint) {
    return new Saved(id(id), name(name), data(data), timepoint(timepoint));
  }
  static ID id(UUID value) { return new ID(value); }
  static Name name(String value) {
    return new Name(value);
  }
  static Data data(JsonObject value) {
    return new Data(value);
  }
  static Timepoint timepoint(LocalDateTime value) { return new Timepoint(value); }

  record ID(UUID value) {}
  record Name(String value) {}
  record Data(JsonObject value) {}
  record Timepoint(LocalDateTime value) {}
}
