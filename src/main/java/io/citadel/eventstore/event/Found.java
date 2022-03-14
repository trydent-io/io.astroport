package io.citadel.eventstore.event;

import io.citadel.shared.context.Domain;
import io.citadel.shared.func.Maybe;

import java.util.stream.Stream;

import static io.citadel.eventstore.EventStore.EventInfo;

public final class Found implements Events {
  private final long version;
  private final Stream<EventInfo> stream;

  public Found(long version, Stream<EventInfo> stream) {
    this.version = version;
    this.stream = stream;
  }

  @Override
  public <A extends Domain.Aggregate<?>> Maybe<A> aggregate(Domain.Hydration<A> hydration) {
    return Domain.Version.of(version).map(it -> hydration.apply(it, stream));
  }
}
