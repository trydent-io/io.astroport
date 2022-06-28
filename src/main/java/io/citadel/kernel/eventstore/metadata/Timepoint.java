package io.citadel.kernel.eventstore.metadata;

import java.time.LocalDateTime;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

public record Timepoint(LocalDateTime value) {
  public Timepoint {
    assert value != null;
  }
  public String asIsoDateTime() {
    return value.format(ISO_DATE_TIME);
  }

  public static Timepoint of(LocalDateTime value) {
    return switch (value) {
      case null -> throw new IllegalArgumentException("Value can't be null");
      default -> new Timepoint(value);
    };
  }
}
