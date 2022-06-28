package io.citadel.kernel.domain.model;

import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.eventstore.metadata.Change;
import io.citadel.kernel.eventstore.metadata.Entity;
import io.vertx.core.json.JsonObject;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public final class Local implements Domain.Model {
  private final Entity entity;
  private final Stream<Change> events;

  public Local(Entity entity, Stream<Change> events) {
    this.entity = entity;
    this.events = events;
  }

  @Override
  public <E> Domain.Model deserialize(BiFunction<? super String, ? super JsonObject, ? extends E> deserializer) {
    return new Deserialized<>(entity, events.map(it -> deserializer.apply(it.name(), it.data())));
  }
}

final class Deserialized<T> implements Domain.Model {
  private final Entity entity;
  private final Stream<T> events;

  Deserialized(Entity entity, Stream<T> events) {
    this.entity = entity;
    this.events = events;
  }

  @Override
  public <E> Domain.Model deserialize(BiFunction<? super String, ? super JsonObject, ? extends E> deserializer) {
    return this;
  }

  @Override
  public <R extends Record> Domain.Model initialize(Function<? super I, ? extends R> initializer) {
    return new Initialized<>(initializer.apply(entity.id().value()), events);
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
