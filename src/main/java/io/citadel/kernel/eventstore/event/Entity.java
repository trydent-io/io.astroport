package io.citadel.kernel.eventstore.event;

public sealed interface Entity {
  record None(ID id, Name name) implements Entity {}
  record One(ID id, Name name, Version version) implements Entity {}

  static Entity none(ID id, Name name) {
    return new None(id, name);
  }
  static Entity one(String id, String name, long version) {
    return new One(id(id), name(name), version(version));
  }
  static ID id(String value) { return new ID(value); }
  static Name name(String value) { return new Name(value); }
  static Version version(long value) { return new Version(value); }
  static Version versionZero() { return new Version(0); }
  record ID(String value) {}
  record Name(String value) {}
  record Version(long value) {}
}
