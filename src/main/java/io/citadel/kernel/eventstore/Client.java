package io.citadel.kernel.eventstore;

import io.citadel.kernel.eventstore.event.Audit;
import io.citadel.kernel.eventstore.event.Entity;
import io.citadel.kernel.vertx.Task;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.templates.SqlTemplate;

import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;

final class Client implements EventStore, Query, Update, Task {
  private final EventBus eventBus;
  private final SqlClient client;

  Client(EventBus eventBus, SqlClient client) {
    this.eventBus = eventBus;
    this.client = client;
  }

  @Override
  public Future<Stream<Audit>> restore(Entity.ID id, Entity.Name name) {
    return SqlTemplate.forQuery(client, queryTemplate)
      .mapTo(Audit::fromRow)
      .execute(with(id, name, Entity.Version.zero()))
      .map(rows -> stream(rows.spliterator(), false));
  }

  @Override
  public Future<Void> store(Stream<Audit> audits) {
    return SqlTemplate.forUpdate(client, updateTemplate)
      .mapTo(Audit::fromRow)
      .execute(with(audits))
      .map(it -> stream(it.spliterator(), false))
      .onSuccess(changes ->
        changes.forEach(change ->
          eventBus.publish(
            change.event().name().value(),
            change.event().data().value(),
            new DeliveryOptions()
              .addHeader("entityId", change.entity().id().value())
              .addHeader("timepoint", change.event().timepoint().value().toString())
              .addHeader("eventId", change.event().id().value().toString())
          )
        )
      )
      .mapEmpty();
  }
}
