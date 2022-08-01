package io.citadel.kernel.eventstore.event;

import io.vertx.sqlclient.Row;

public record Audit(Entity entity, Event event) {
  public Audit { assert entity != null && event != null; }

  public static Audit of(Entity entity, Event event) { return new Audit(entity, event); }

  public static Audit fromRow(Row row) {
    return new Audit(
      Entity.fromRow(row),
      Event.fromRow(row)
    );
  }
}
