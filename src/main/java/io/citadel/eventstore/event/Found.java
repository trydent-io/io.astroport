package io.citadel.eventstore.event;

import io.citadel.kernel.domain.Domain;

import java.util.Optional;
import java.util.stream.Stream;

import io.citadel.eventstore.data.EventInfo;

public final class Found implements Events {
  private final long version;
  private final Stream<EventInfo> stream;

  public Found(long version, Stream<EventInfo> stream) {
    this.version = version;
    this.stream = stream;
  }

  @Override
  public <A extends Domain.Aggregate<?>> Optional<A> aggregateFrom(Domain.Hydration<A> hydration) {
    try {
      return Optional.of(hydration.aggregate(version, stream));
    } catch (Throwable e) {
      return Optional.empty();
    }
  }
}
