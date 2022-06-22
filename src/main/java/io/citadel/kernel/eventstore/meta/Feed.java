package io.citadel.kernel.eventstore.meta;

import io.citadel.kernel.domain.Descriptor;
import io.citadel.kernel.domain.State;
import io.vertx.core.Vertx;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

import java.util.UUID;

import static java.util.stream.StreamSupport.stream;

public interface Feed {
  record Log(UUID id, Entity entity, Event event, Timepoint timepoint) {
    public static Log fromRow(Row row) {
      return new Log(
        row.getUUID("id"),
        Entity.of(row.getString("entity_id"), row.getString("entity_name"), row.getLong("entity_version")),
        Event.of(row.getString("event_name"), row.getJsonObject("event_data")),
        Timepoint.of(row.getLocalDateTime("timepoint"))
      );
    }
  }

  static Feed fromLogs(RowSet<Log> rows) {
    return new Found(stream(rows.spliterator(), false));
  }

  <ID, R extends Record, E, S extends Enum<S> & State<S, E>> Aggregate<R, E> aggregate(Vertx vertx, Descriptor<ID, R, E, S> descriptor);
}
