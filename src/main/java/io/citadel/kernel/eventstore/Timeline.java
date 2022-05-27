package io.citadel.kernel.eventstore;

import io.citadel.kernel.domain.Domain.Archetype;
import io.citadel.kernel.domain.Domain.Model;
import io.citadel.kernel.func.ThrowableTriFunction;

import java.util.stream.Stream;

public final class Timeline {
  private final Feed.Aggregate aggregate;
  private final Stream<Feed.Event> events;

  Timeline(Feed.Aggregate aggregate, Stream<Feed.Event> events) {
    this.aggregate = aggregate;
    this.events = events;
  }

  public Timeline aggregate(Feed.Aggregate aggregate) {
    return this.aggregate.version() == -1 ? new Timeline(aggregate, events) : this;
  }

  public Timeline append(Feed.Event event) {
    return new Timeline(aggregate, Stream.concat(events, Stream.of(event)));
  }

  public <M extends Record & Model<?>> Archetype<? extends M> archetype(ThrowableTriFunction<? super String, ? super String, ? super Long, ? extends Archetype<? extends M>> archetype) {
    return archetype.apply(aggregate.id(), aggregate.name(), aggregate.version());
  }
}
