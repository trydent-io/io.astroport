package io.citadel.eventstore.data;

import io.citadel.kernel.media.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;

import java.util.stream.Stream;

public record MetaEvent(String name, JsonObject data) {
  public static MetaEvent from(JsonObject json) {
    return fromJson(json);
  }

  public static MetaEvent fromJson(JsonObject json) {
    return new MetaEvent(
      json.getString("name"),
      json.getJsonObject("data")
    );
  }

  public static Stream<MetaEvent> fromJsonArray(JsonArray array) {
    return array.stream()
      .map(Json::fromAny)
      .map(MetaEvent::fromJson);
  }

  public static MetaEvent from(Row row) {
    return new MetaEvent(
      row.getString("event_name"),
      row.getJsonObject("event_data")
    );
  }

}
