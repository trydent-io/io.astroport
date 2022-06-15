package io.citadel.kernel.eventstore.meta;

import java.util.function.Function;

public record ID(String value) {
  public ID {
    assert value != null;
  }
  public <T> T as(Function<? super String, ? extends T> converter) {
    return value instanceof T t ? t : null;
  }
}
