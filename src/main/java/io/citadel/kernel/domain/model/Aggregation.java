package io.citadel.kernel.domain.model;

import io.citadel.eventstore.data.Feed;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.eventstore.EventStore;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

public final class Aggregation<M extends Record & Domain.Model<?>, A extends Domain.Aggregate> implements Domain.Snapshot<A> {
  private final EventStore eventStore;
  private final M model;
  private final Predicate<? super M> verify;
  private final AtomicReference<Domain.Snapshot<A>> reference;

  public Aggregation(final EventStore eventStore, final M model, final Predicate<? super M> verify, final AtomicReference<Domain.Snapshot<A>> reference) {
    this.eventStore = eventStore;
    this.model = model;
    this.verify = verify;
    this.reference = reference;
  }

  @Override
  public BiConsumer<Domain.Snapshot<A>, Feed.Entry> accumulator() {
    return (s, entry) -> s.apply();
  }

  @Override
  public Function<Domain.Snapshot<A>, Optional<A>> finisher() {
    return null;
  }
}
