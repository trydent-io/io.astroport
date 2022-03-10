package io.citadel.eventstore;

import io.citadel.eventstore.Entries.Aggregate;
import io.citadel.eventstore.Entries.Event;
import io.citadel.eventstore.Entries.Entry;
import io.citadel.eventstore.Operations.FoundEvents;
import io.vertx.core.Future;
import io.vertx.core.Verticle;

import java.util.stream.Stream;

public sealed interface EventStore permits Client, Forward, Service {
  Operations operations = Operations.Defaults;
  Defaults defaults = Defaults.Companions;
  Entries entries = Entries.Defaults;

  default Verticle asVerticle() {
    return switch (this) {
      case Service service -> service;
      default -> null;
    };
  }
  Future<FoundEvents> findEventsBy(Aggregate aggregate);
  Future<Stream<Entry>> persist(Aggregate aggregate, Stream<Event> events);
}
