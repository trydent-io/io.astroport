package io.citadel.kernel.eventstore;

import io.citadel.kernel.media.Json;
import io.citadel.kernel.vertx.Task;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.templates.SqlTemplate;

import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;

public interface EventStore {
  static EventStore client(Vertx vertx, SqlConnectOptions options) {
    return new Client(vertx.eventBus(), PgPool.client(vertx, PgConnectOptions.wrap(options), new PoolOptions().setMaxSize(10)));
  }
  Future<Stream<Audit>> restore(Audit.Entity entity);

  Future<Void> store(Stream<Audit> entityEvents);
}

final class Client implements EventStore, Query, Update, Task {
  private final EventBus eventBus;
  private final SqlClient client;

  Client(EventBus eventBus, SqlClient client) {
    this.eventBus = eventBus;
    this.client = client;
  }

  @Override
  public Future<Stream<Audit>> restore(Audit.Entity entity) {
    return SqlTemplate.forQuery(client, queryTemplate)
      .mapTo(Audit::fromRow)
      .execute(
        Map.of(
          "entityId", entity.id(),
          "entityName", entity.name(),
          "entityVersion", entity.version()
        )
      )
      .map(rows -> stream(rows.spliterator(), false));
  }

  @Override
  public Future<Void> store(Stream<Audit> audits) {
    return SqlTemplate.forUpdate(client, updateTemplate)
      .mapTo(Audit::fromRow)
      .execute(Map.of("events", Json.array(audits)))
      .map(it -> stream(it.spliterator(), false))
      .onSuccess(changes ->
        changes.forEach(change ->
          eventBus.publish(
            change.event().name(),
            change.event().data(),
            new DeliveryOptions()
              .addHeader("entityId", change.entity().id())
              .addHeader("timepoint", change.event().timepoint().toString())
              .addHeader("eventId", change.event().id().toString())
          )
        )
      )
      .mapEmpty();
  }
}
