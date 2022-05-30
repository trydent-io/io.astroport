package io.citadel.kernel.eventstore;

import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.domain.Domain.Model;
import io.citadel.kernel.func.ThrowableBiFunction;
import io.citadel.kernel.func.ThrowableFunction;
import io.vertx.core.json.JsonObject;

import java.util.stream.Stream;

public final class Timeline<ID extends Domain.ID<?>> {
  private final Meta.Aggregate<ID> aggregate;
  private final Stream<Meta.Event> events;

  public Timeline(ID aggregateId, String aggregateName, long aggregateVersion) {
    this(new Meta.Aggregate<>(aggregateId, aggregateName, aggregateVersion), Stream.empty());
  }
  private Timeline(Meta.Aggregate<ID> aggregate, Stream<Meta.Event> events) {
    this.aggregate = aggregate;
    this.events = events;
  }

  public Timeline<ID> aggregate(Meta.Aggregate<ID> aggregate) {
    return this.aggregate.version() == -1 ? new Timeline<>(aggregate, events) : this;
  }

  public Timeline<ID> append(Meta.Event event) {
    return new Timeline<>(aggregate, Stream.concat(events, Stream.of(event)));
  }

  public <M extends Record & Model<ID>, E extends Domain.Event> Identity<M, E> identity(ThrowableFunction<? super ID, ? extends M> asModel, ThrowableBiFunction<? super String, ? super JsonObject, ? extends E> asEvent) {
    return new Archetype<>(
      asModel.apply(aggregate.id()),
      aggregate.name(),
      aggregate.version(),
      events.map(event -> asEvent.apply(event.name(), event.data()))
    );
  }

}
