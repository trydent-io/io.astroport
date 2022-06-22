package io.citadel.kernel.eventstore.meta;

public record Version(long value) {
  private static final Version DEFAULT = new Version(0);

  public Version {
    assert value >= 0;
  }

  public boolean isDefault() {
    return this.equals(DEFAULT);
  }

  static Version of(Long value) {
    return switch (value) {
      case null -> throw new IllegalArgumentException("Version can't be null");
      case Long it && it < 0 -> throw new IllegalArgumentException("Version can't be less than 0");
      default -> new Version(value);
    };
  }
}
