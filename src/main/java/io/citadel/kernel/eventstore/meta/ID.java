package io.citadel.kernel.eventstore.meta;

public record ID<T>(T value) {
  public ID {
    assert value != null;
  }
}
