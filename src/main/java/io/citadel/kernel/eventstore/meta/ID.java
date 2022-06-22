package io.citadel.kernel.eventstore.meta;

import java.util.function.Function;

public record ID(String value) {
  public ID {
    assert value != null;
  }
  public <T> T as(Function<? super String, ? extends T> deserializer) {
    return deserializer.apply(value);
  }

  static ID of(String value) {
    return switch (value) {
      case null -> throw new IllegalArgumentException("ID can't be null");
      default -> new ID(value.toString());
    };
  }
}
