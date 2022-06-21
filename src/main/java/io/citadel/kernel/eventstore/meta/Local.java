package io.citadel.kernel.eventstore.meta;

import java.util.stream.Stream;

final class Local implements Feed {
  private final Stream<Log> logs;

  Local(Stream<Log> logs) {
    this.logs = logs;
  }
}
