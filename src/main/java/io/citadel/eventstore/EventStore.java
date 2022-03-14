package io.citadel.eventstore;

import io.citadel.eventstore.Entries.Entry;
import io.citadel.eventstore.Entries.Event;
import io.citadel.shared.context.Domain.Version;
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
  default Future<Events> findEventsBy(String id, String name) {return findEventsBy(id, name, Version.last().value()); }
  Future<Events> findEventsBy(String id, String name, long version);
  Future<Stream<Entry>> persist(Entries.Aggregate aggregate, Stream<Event> events);

}
