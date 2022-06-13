package io.citadel.kernel.eventstore.meta;

public record Name(String value) {
  public Name {
    assert value != null && !value.isEmpty() && !value.isBlank();
  }
}
