package io.citadel.kernel.eventstore;

import io.citadel.kernel.eventstore.event.Entity;
import io.citadel.kernel.eventstore.event.EntityEvent;
import io.citadel.kernel.eventstore.event.Event;
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
  public Future<Stream<EntityEvent>> restore(Entity.ID id, Entity.Name name) {
    return SqlTemplate.forQuery(client, queryTemplate)
      .mapTo(EntityEvent::last)
      .execute(params(id, name, Entity.versionZero()))
      .map(rows -> stream(rows.spliterator(), false))
      .compose(filter(it -> it.findAny().isPresent(), Stream.of(EntityEvent.identity(id, name))));
  }

  @Override
  public Future<Void> store(Entity.ID id, Entity.Name name, Entity.Version version, Stream<Event> events) {
    return SqlTemplate.forUpdate(client, updateTemplate).mapTo(row ->
        Event.saved(
          row.getUUID("event_id"),
          row.getString("event_name"),
          row.getJsonObject("event_data"),
          row.getLocalDateTime("event_timepoint")
        )
      )
      .execute(params(id, name, version, events))
      .map(events -> stream(events.spliterator(), false))
      .onSuccess(events ->
        events.forEach(change ->
          eventBus.publish(change.eventName().value(), change.eventData().value(), new DeliveryOptions()
            .addHeader("aggregateId", change.aggregateId().value())
            .addHeader("timepoint", change.timepoint().asIsoDateTime())
          )
        )
      )
      .mapEmpty();
  }
}
