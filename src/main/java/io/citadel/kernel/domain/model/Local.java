package io.citadel.kernel.domain.model;

import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.eventstore.meta.Aggregate;
import io.citadel.kernel.eventstore.meta.Event;
import io.vertx.core.json.JsonObject;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public final class Local implements Domain.Model {
  private final Aggregate aggregate;
  private final Stream<Event> events;

  public Local(Aggregate aggregate, Stream<Event> events) {
    this.aggregate = aggregate;
    this.events = events;
  }

  @Override
  public <E> Domain.Model deserialize(BiFunction<? super String, ? super JsonObject, ? extends E> deserializer) {
    return new Deserialized<>(aggregate, events.map(it -> deserializer.apply(it.name(), it.data())));
  }

  @Override
  public <R extends Record> Domain.Model initialize(Function initializer) {
    return this;
  }
}

final class Deserialized<T> implements Domain.Model {
  private final Aggregate aggregate;
  private final Stream<T> events;

  Deserialized(Aggregate aggregate, Stream<T> events) {
    this.aggregate = aggregate;
    this.events = events;
  }

  @Override
  public <E> Domain.Model deserialize(BiFunction<? super String, ? super JsonObject, ? extends E> deserializer) {
    return this;
  }

  @Override
  public <R extends Record> Domain.Model initialize(Function<? super I, ? extends R> initializer) {
    return new Initialized<>(initializer.apply(aggregate.id().value()), events);
  }
}

final class Initialized<R extends Record, T> implements Domain.Model {
  private final R aggregate;
  private final Stream<T> events;

  Initialized(R aggregate, Stream<T> events) {
    this.aggregate = aggregate;
    this.events = events;
  }

  @Override
  public <E> Domain.Model deserialize(BiFunction<? super String, ? super JsonObject, ? extends E> deserializer) {
    return this;
  }
}
