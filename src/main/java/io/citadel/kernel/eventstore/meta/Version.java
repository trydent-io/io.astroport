package io.citadel.kernel.eventstore.meta;

public record Version(long value) {
  private static final Version DEFAULT = new Version(0);

  public Version {
    assert value >= 0;
  }

  public boolean isDefault() {
    return this.equals(DEFAULT);
  }
}
