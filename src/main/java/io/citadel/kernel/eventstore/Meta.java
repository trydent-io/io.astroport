package io.citadel.kernel.eventstore;

import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.lang.Iterators;
import io.citadel.kernel.media.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface Meta extends Iterable<Meta.Log> {
  Meta EMPTY = new Type.Entries();

  record Aggregate<ID extends Domain.ID<?>>(ID id, String name, long version) {}
  record Timepoint(LocalDateTime dateTime) {}
  record Event(String name, JsonObject data, Timepoint timepoint) {}

  record Persisted(LocalDateTime at, String by) {}
  record Log(UUID id, Aggregate aggregate, Event event, Persisted persisted) {
    public Log(Aggregate aggregate, Event event) {
      this(null, aggregate, event, null);
    }
    public Log(Aggregate aggregate) {
      this(null, aggregate, null, null);
    }
  }

  static Meta fromJson(JsonObject json) {
    return from(json.getJsonArray("entries"));
  }

  static Meta empty(String aggregateId, String aggregateName) {
    return new Type.Entries(Meta.archetype(aggregateId, aggregateName));
  }

  static Log archetype(String aggregateId, String aggregateName) {
    return new Log(new Aggregate(aggregateId, aggregateName));
  }

  static Meta from(JsonArray array) {
    return new Type.Entries(array.stream()
      .map(Json::fromAny)
      .map(it -> it.mapTo(Log.class))
      .toArray(Log[]::new));
  }

  static Meta fromRows(RowSet<Row> rows) {
    return from(Json.array(rows));
  }

  default Stream<Log> stream() {
    return StreamSupport.stream(this.spliterator(), false);
  }

  default JsonArray asJsonArray() {
    return Json.array(this);
  }

  enum Type {;
    private record Empty(Log log) implements Meta {

      @Override
      public Iterator<Log> iterator() {
        return Iterators.defaults.empty();
      }
    }
    private record Entries(Log... entries) implements Meta {
      @Override
      public Iterator<Log> iterator() {
        return List.of(entries).iterator();
      }
    }
  }
}
