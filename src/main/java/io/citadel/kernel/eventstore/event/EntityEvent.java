package io.citadel.kernel.eventstore.event;

import io.vertx.sqlclient.Row;

public sealed interface EntityEvent {
  static EntityEvent zero(Entity.ID id, Entity.Name name) {
    return new Zero(new Entity.None(id, name));
  }

  static EntityEvent last(Row row) {
    return new Last(
      Entity.one(
        row.getString("entity_id"),
        row.getString("entity_name"),
        row.getLong("entity_version")
      ),
      Event.saved(
        row.getUUID("event_id"),
        row.getString("event_name"),
        row.getJsonObject("event_data"),
        row.getLocalDateTime("event_timepoint")
      )
    );
  }

  record Zero(Entity entity) implements EntityEvent {
  }
  record Last(Entity entity, Event event) implements EntityEvent {
  }
}
