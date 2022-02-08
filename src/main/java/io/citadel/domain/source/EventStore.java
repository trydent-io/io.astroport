package io.citadel.domain.source;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlClient;

import java.util.UUID;

public interface EventStore {
  Operations operations = Operations.Defaults;
  Events events = Events.Defaults;

  static EventStore service(SqlClient sqlClient) {
    return new Service(new Sql(sqlClient));
  }

  Future<StoredEvents> findBy(UUID id);
  void persist(StoredEvents.Stored... events);

  final class Sql implements EventStore {
    private final SqlClient sqlClient;

    private Sql(final SqlClient sqlClient) {this.sqlClient = sqlClient;}

    @Override
    public Future<StoredEvents> findBy(final UUID id) {
      return null;
    }

    @Override
    public void persist(final StoredEvents.Stored... events) {

    }
  }

  final class Service extends AbstractVerticle implements EventStore {
    private final EventStore store;

    public Service(final EventStore store) {this.store = store;}

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

    @Override
    public Future<StoredEvents> findBy(final UUID id) {
      return store.findBy(id);
    }

    @Override
    public void persist(final StoredEvents.Stored... events) {
      store.persist(events);
    }
  }
}
