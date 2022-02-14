package io.citadel.domain.source;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Verticle;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlClient;

import java.util.UUID;

public interface EventStore extends Verticle {
  Operations operations = Operations.Defaults;
  Events events = Events.Defaults;

  static EventStore service(SqlClient sqlClient) {
    return new Repository(new Sql(sqlClient));
  }

  Future<EventLogs> findBy(UUID id);
  void persist(EventLogs.Stored... events);

  final class Repository extends AbstractVerticle implements EventStore {
    private final EventLogs eventLogs;

    public Repository(final EventLogs eventLogs) {this.eventLogs = eventLogs;}

    @Override
    public void start(final Promise<Void> start) {
      vertx.eventBus().<JsonObject>localConsumer(operations.RESTORE, message ->
        findBy(message.body().mapTo(UUID.class))
          .onSuccess(message::reply)
          .onFailure(it -> message.fail(500, it.getMessage()))
      );

      vertx.eventBus().<JsonObject>localConsumer(operations.PERSIST, message ->
        persist(message.body().mapTo())
      );

      start.complete();
    }
  }
}
