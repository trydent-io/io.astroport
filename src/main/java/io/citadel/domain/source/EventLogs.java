package io.citadel.domain.source;

import io.vertx.core.Future;
import org.eclipse.persistence.sessions.Session;

import java.util.UUID;
import java.util.stream.Stream;

public interface EventLogs {
  Future<Stream<EventLog>> findBy(UUID id, String aggregateName);
  Future<Void> persist(EventLog... eventLogs);

  final class EclipseLink implements EventLogs {
    private final Session session;

    private EclipseLink(Session session) {
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
