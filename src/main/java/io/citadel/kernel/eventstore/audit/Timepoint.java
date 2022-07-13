package io.citadel.kernel.eventstore.audit;

import java.time.Instant;
import java.time.LocalDateTime;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

public record Timepoint(Instant value) {
  public Timepoint {
    assert value != null;
  }
  public String asIsoDateTime() {
    return LocalDateTime.from(value).format(ISO_DATE_TIME);
  }

  public static Timepoint of(Instant value) {
    return switch (value) {
      case null -> throw new IllegalArgumentException("Value can't be null");
      default -> new Timepoint(value);
    };
  }
}
