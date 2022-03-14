package io.citadel.eventstore.events;

import io.citadel.eventstore.Entries;
import io.citadel.eventstore.Events;
import io.citadel.shared.context.Domain;
import io.citadel.shared.func.Maybe;

import java.util.Objects;
import java.util.stream.Stream;

public final class Found implements Events {
  private final long version;
  private final Stream<Entries.Event> stream;

  public Found(long version, Stream<Entries.Event> stream) {
    this.version = version;
    this.stream = stream;
  }

  @Override
  public <A extends Domain.Aggregate<?>> Maybe<A> aggregate(Domain.Hydration<A> hydration) {
    return Domain.Version.of(version).map(it -> hydration.apply(it, stream));
  }
}
