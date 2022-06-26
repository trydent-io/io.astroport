package io.citadel.kernel.eventstore.meta;

public record Version(long value) {
  public static final Version Zero = new Version(0);
  public static final Version Last = new Version(Long.MAX_VALUE);

  public Version {
    assert value >= 0;
  }

  public boolean isDefault() {
    return this.equals(Zero);
  }

  static Version of(Long value) {
    return switch (value) {
      case null -> throw new IllegalArgumentException("Version can't be null");
      case Long it && it < 0 -> throw new IllegalArgumentException("Version can't be less than 0");
      default -> new Version(value);
    };
  }
}
