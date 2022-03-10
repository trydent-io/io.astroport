package io.citadel.eventstore;

import java.util.stream.Stream;

import static io.citadel.eventstore.Entries.*;

public enum Operations {
  Defaults;

  public static final String FIND_BY_AGGREGATE = "eventStore.findByAggregate";
  public static final String PERSIST_AGGREGATE_EVENTS = "eventStore.persistAggregateEvents";

  public FindBy findBy(Aggregate aggregate) {
    return new FindBy(aggregate);
  }

  public Persist persist(Aggregate aggregate, Stream<Event> events) {
    return new Persist(aggregate, events);
  }

  public record FindBy(Aggregate aggregate) {}
  public record FoundEvents(long version, Stream<Event> events) {}
  public record Persist(Aggregate aggregate, Stream<Event> events) {}
  public record Persisted(Aggregate aggregate, Stream<Event> events) {}
}
