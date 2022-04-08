package io.citadel.eventstore.event;

import io.citadel.kernel.domain.Domain;

import java.util.Optional;
import java.util.stream.Stream;

import io.citadel.eventstore.data.EventInfo;

public final class Found implements Events {
  private final String id;
  private final long version;
  private final Stream<EventInfo> stream;

  public Found(final String id, long version, Stream<EventInfo> stream) {
    this.id = id;
    this.version = version;
    this.stream = stream;
  }

  @Override
  public <A extends Domain.Aggregate> Optional<A> aggregateFrom(Domain.Snapshot<A> snapshot) {
    try {
      return Optional.of(snapshot.aggregate(id, version, stream));
    } catch (Throwable e) {
      return Optional.empty();
    }
  }
}
