package io.citadel.kernel.eventstore.event;

import io.vertx.sqlclient.Row;

public record Entity(ID id, Name name, Version version) {
  public Entity { assert id != null && name != null && version != null; }
  public static Entity zero(ID id, Name name) {
    return new Entity(id, name, new Version(0));
  }
  static Entity versioned(String id, String name, long version) {
    return new Versioned(id(id), name(name), version(version));
  }

  public static Entity fromRow(Row row) {
    return new Entity(
      id(row.getString("entity_id")),
      name(row.getString("entity_name")),
      version(row.getLong("entity_version"))
    );
  }
  public static ID id(String value) { return new ID(value); }
  public static Name name(String value) { return new Name(value); }
  public static Version version(long value) { return new Version(value); }
  static Version versionZero() { return new Version(0); }
  public record ID(String value) {}
  public record Name(String value) {}
  public record Version(long value) {
    public Version { assert value >= 0; }
    public static Version zero() { return new Version(0); }
  }
}
