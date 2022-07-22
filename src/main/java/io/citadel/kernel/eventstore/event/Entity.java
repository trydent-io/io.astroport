package io.citadel.kernel.eventstore.event;

public sealed interface Entity {
  record Unversioned(ID id, Name name) implements Entity {}
  record Versioned(ID id, Name name, Version version) implements Entity {}

  static Entity unversioned(ID id, Name name) {
    return new Unversioned(id, name);
  }
  static Entity versioned(String id, String name, long version) {
    return new Versioned(id(id), name(name), version(version));
  }
  static ID id(String value) { return new ID(value); }
  static Name name(String value) { return new Name(value); }
  static Version version(long value) { return new Version(value); }
  static Version versionZero() { return new Version(0); }
  record ID(String value) {}
  record Name(String value) {}
  record Version(long value) {}
}
