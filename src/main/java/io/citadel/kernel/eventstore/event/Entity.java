package io.citadel.kernel.eventstore.event;

public sealed interface Entity {
  record None(ID id, Name name) implements Entity {}
  record Done(ID id, Name name, Version version) implements Entity {}

  static Entity none(ID id, Name name) {
    return new None(id, name);
  }
  static Entity done(String id, String name, long version) {
    return new Done(id(id), name(name), version(version));
  }
  static ID id(String value) { return new ID(value); }
  static Name name(String value) { return new Name(value); }
  static Version version(long value) { return new Version(value); }
  record ID(String value) {}
  record Name(String value) {}
  record Version(long value) {
    Version() { this(0); }
  }
}
