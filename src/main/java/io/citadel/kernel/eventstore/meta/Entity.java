package io.citadel.kernel.eventstore.meta;

public sealed interface Entity {

  public static <T> Entity of(T id, String name, long version) {
    return new Entity(id(id), name(name), version(version));
  }

  public static Entity of(ID id, Name name, Version version) {
    return new Entity(id, name, version);
  }

  public static <T> ID id(T value) {
    return switch (value) {
      case null -> throw new IllegalArgumentException("ID can't be null");
      default -> new ID(value.toString());
    };
  }

  static Name name(String value) {
    return Name.of(value);
  }

  public static Version version(Long value) {
    return switch (value) {
      case null -> throw new IllegalArgumentException("Version can't be null");
      case Long it && it < 0 -> throw new IllegalArgumentException("Version can't be less than 0");
      default -> new Version(value);
    };
  }

  Aggregate<R> aggregate()
}

final class Meta implements Entity {

}
