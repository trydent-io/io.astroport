package io.citadel.kernel.eventstore.event;

import io.vertx.sqlclient.Row;

public sealed interface EntityEvent {
  static EntityEvent zero(Entity.ID id, Entity.Name name) {
    return new Zero(Entity.none(id, name));
  }

  static EntityEvent last(Row row) {
    return new Last(
      new Entity.Done(
        Entity.id(row.getString("entity_id")),
        Entity.name(row.getString("entity_name")),
        Entity.version(row.getLong("entity_version"))
      ),
      new Event.Saved(
        Event.id(row.getUUID("event_id")),
        Event.name(row.getString("event_name")),
        Event.data(row.getJsonObject("event_data")),
        Event.timepoint(row.getLocalDateTime("event_timepoint"))
      )
    );
  }

  record Zero(Entity entity) implements EntityEvent {
  }
  record Last(Entity entity, Event event) implements EntityEvent {
  }
}
