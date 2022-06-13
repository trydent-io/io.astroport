package io.citadel.kernel.eventstore.meta;

public record Aggregate(ID<?> id, Name name, Version version) {
  public Aggregate {
    assert id != null && name != null && version != null;
  }

  public static <T> Aggregate of(T id, String name, long version) {
    return new Aggregate(id(id), name(name), version(version));
  }

  public static Aggregate of(ID<?> id, Name name, Version version) {
    return new Aggregate(id, name, version);
  }

  public static <T> ID<T> id(T value) {
    return switch (value) {
      case null -> throw new IllegalArgumentException("ID can't be null");
      default -> new ID<>(value);
    };
  }

  public static Name name(String value) {
    return switch (value) {
      case null -> throw new IllegalArgumentException("Name can't be null");
      case String it && (it.isEmpty() || it.isBlank()) -> throw new IllegalArgumentException("Name can't be empty or blank");
      default -> new Name(value);
    };
  }

  public static Version version(Long value) {
    return switch (value) {
      case null -> throw new IllegalArgumentException("Version can't be null");
      case Long it && it < 0 -> throw new IllegalArgumentException("Version can't be less than 0");
      default -> new Version(value);
    };
  }
}
