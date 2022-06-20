package io.citadel.kernel.eventstore.meta;

import io.citadel.kernel.media.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface Meta extends Iterable<Meta.Log> {

  record Log(UUID id, Entity entity, Event event, Timepoint timepoint) {
  }

  sealed interface Aggregate {

  }

  static Meta from(JsonArray array) {
    return new Type.Entries(array.stream()
      .map(Json::fromAny)
      .map(it -> it.mapTo(Log.class))
      .toArray(Log[]::new)
    );
  }

  static Meta fromRows(RowSet<Row> rows) {
    return from(Json.array(rows));
  }

  default Stream<Log> stream() {
    return StreamSupport.stream(this.spliterator(), false);
  }

  enum Type {
    ;

    private record Entries(Log... entries) implements Meta {
      @Override
      public Iterator<Log> iterator() {
        return List.of(entries).iterator();
      }
    }
  }
}
