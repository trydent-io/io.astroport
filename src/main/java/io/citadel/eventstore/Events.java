package io.citadel.eventstore;

import io.citadel.eventstore.events.Empty;
import io.citadel.eventstore.events.Found;
import io.citadel.shared.context.Domain;
import io.citadel.shared.func.Maybe;

import java.util.stream.Stream;

public sealed interface Events permits Empty, Found {
  static Events found(long version, Stream<Entries.Event> events) {return new Found(version, events);}
  static Events empty() { return Empty.Default; }

  default <A extends Domain.Aggregate<?>> Maybe<A> aggregate(Domain.Hydration<A> hydration) { return Maybe.empty(); }
}
