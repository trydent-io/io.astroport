package io.citadel.kernel.eventstore.event;

import java.util.stream.Stream;

import io.citadel.kernel.eventstore.data.EventInfo;

public final class Found implements Events {
  private final String id;
  private final long version;
  private final Stream<EventInfo> stream;

  public Found(final String id, long version, Stream<EventInfo> stream) {
    this.id = id;
    this.version = version;
    this.stream = stream;
  }

}
