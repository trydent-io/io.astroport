package io.citadel.kernel.eventstore.meta;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

public record Timepoint(LocalDateTime value) {
  public Timepoint {
    assert value != null;
  }
  public String asIsoDateTime() {
    return value.format(ISO_DATE_TIME);
  }
}
