package io.citadel.kernel.media;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;

public sealed interface Json {
  enum Namespace implements Json {}

  static JsonObject of(Object... fields) {
    final var json = new JsonObject();
    for (var index = 0; index < fields.length; index += 2) {
      json.put(fields[index].toString(), fields[index + 1]);
    }
    return json;
  }

  static JsonObject parse(String value) {
    return new JsonObject(value);
  }

  static <T> JsonObject fromAny(T any) {
    return JsonObject.mapFrom(any);
  }

  static <T> JsonObject with(T any) {
    return fromAny(any);
  }

  static JsonArray array(JsonObject... jsons) {
    return new JsonArray(List.of(jsons));
  }

  static JsonArray array(RowSet<Row> rows) {
    return Json.array(
      stream(rows.spliterator(), false)
        .map(Row::toJson)
        .toArray(JsonObject[]::new)
    );
  }

  static <T> JsonArray array(Iterable<T> itearable) {
    return array(stream(itearable.spliterator(), false));
  }

  static <T> JsonArray array(Stream<T> items) {
    return Json.array(items.map(Json::fromAny).toArray(JsonObject[]::new));
  }
}
