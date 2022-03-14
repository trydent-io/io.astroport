package io.citadel.shared.context.hydration;

import io.citadel.eventstore.Entries;
import io.citadel.shared.context.Domain;

import java.util.stream.Stream;

public final class Aggregate<A extends Domain.Aggregate<?>> implements Domain.Hydration<A> {
  private final

  @Override
  public A tryApply(final Domain.Version version, final Stream<Entries.Event> eventStream) {
    return null;
  }
}
