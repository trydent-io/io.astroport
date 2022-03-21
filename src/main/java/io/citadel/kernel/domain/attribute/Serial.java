package io.citadel.kernel.domain.attribute;

import io.citadel.kernel.domain.Domain;

public record Serial(long value) implements Domain.Version {
  public Serial {
    if (value < 0)
      throw new IllegalArgumentException("Version can't be less than 0");
  }
}
