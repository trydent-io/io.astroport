package io.citadel.eventstore.data;

import io.citadel.kernel.media.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;

import java.util.stream.Stream;

public record EventInfo(String name, JsonObject data) {
  public static EventInfo from(JsonObject json) {
    return fromJson(json);
  }

  public static EventInfo fromJson(JsonObject json) {
    return new EventInfo(
      json.getString("name"),
      json.getJsonObject("data")
    );
  }

  public static Stream<EventInfo> fromJsonArray(JsonArray array) {
    return array.stream()
      .map(Json::fromAny)
      .map(EventInfo::fromJson);
  }

  public static EventInfo from(Row row) {
    return new EventInfo(
      row.getString("event_name"),
      row.getJsonObject("event_data")
    );
  }

}
