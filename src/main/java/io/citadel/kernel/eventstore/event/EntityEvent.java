package io.citadel.kernel.eventstore.event;

import io.vertx.sqlclient.Row;

public sealed interface EntityEvent {
  static EntityEvent identity(Entity.ID id, Entity.Name name) {
    return new Identity(new Entity.Unversioned(id, name));
  }

  static EntityEvent change(Entity entity, Event event) { return new Change(entity, event); }

  static EntityEvent last(Row row) {
    return new Change(
      Entity.versioned(
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

  record Identity(Entity entity) implements EntityEvent {
  }
  record Change(Entity entity, Event event) implements EntityEvent {
  }
}
