package io.citadel.kernel.eventstore;

import io.vertx.core.Future;
import org.eclipse.persistence.sessions.Session;

import java.util.UUID;
import java.util.stream.Stream;

public sealed interface EventLogs {
  Future<Stream<EventLog>> findBy(UUID id, String aggregateName);
  Future<Void> persist(EventLog... eventLogs);

  static EventLogs persistence(Session session) {
    return new Persistence(session);
  }

  final class Persistence implements EventLogs {
    private final Session session;

    private Persistence(Session session) {
      this.session = session;
    }

    @Override
    public Future<Stream<EventLog>> findBy(UUID id, String aggregateName) {
      return null;
    }

    @Override
    public Future<Void> persist(EventLog... eventLogs) {
      return null;
    }
  }
}
