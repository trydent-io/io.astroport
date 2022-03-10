package io.citadel.shared.context.attribute;

import io.citadel.shared.context.Domain;

public record Serial(long value) implements Domain.Version {
  public Serial {
    if (value < 0)
      throw new IllegalArgumentException("Version can't be less than 0");
  }
}
