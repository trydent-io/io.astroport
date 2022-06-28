package io.citadel.kernel.eventstore.metadata;

public record Name(String value) {
  public Name {
    assert value != null && !value.isEmpty() && !value.isBlank();
  }

  public static Name of(String value) {
    return switch (value) {
      case null -> throw new IllegalArgumentException("Name can't be null");
      case String it && (it.isEmpty() || it.isBlank()) -> throw new IllegalArgumentException("Name can't be empty or blank");
      default -> new Name(value);
    };
  }
}
