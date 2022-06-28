package io.citadel.kernel.eventstore;

import io.citadel.kernel.eventstore.metadata.Aggregate;
import io.citadel.kernel.eventstore.metadata.Change;
import io.citadel.kernel.eventstore.metadata.ID;
import io.citadel.kernel.eventstore.metadata.Name;
import io.citadel.kernel.eventstore.metadata.State;
import io.citadel.kernel.eventstore.metadata.Version;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.templates.SqlTemplate;

import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;

final class Client implements EventStorePool, Query, Update {
  private final EventBus eventBus;
  private final SqlClient client;

  Client(EventBus eventBus, SqlClient client) {
    this.eventBus = eventBus;
    this.client = client;
  }

  @SuppressWarnings("DuplicateBranchesInSwitch")
  @Override
  public Future<Aggregate> query(ID id, Name name, Version version) {
    return SqlTemplate.forQuery(client, queryTemplate)
      .mapTo(row -> switch (row.getJsonObject("data")) {
        case null -> Aggregate.identity(id, name);
        case JsonObject it && it.isEmpty() -> Aggregate.identity(id, name);
        default -> Aggregate.entity(
          Aggregate.id(row.getString("id")),
          Aggregate.name(row.getString("name")),
          Aggregate.version(row.getLong("version")),
          Aggregate.state(row.getString("state")),
          Aggregate.model(row.getJsonObject("model"))
        );
      })
      .execute(params(id, name, version))
      .map(rows -> stream(rows.spliterator(), false))
      .map(Stream::findFirst)
      .map(found -> found.orElseGet(() -> Aggregate.identity(id, name)));
  }

  @Override
  public Future<Void> update(ID id, Name name, Version version, State state, Stream<Change> changes) {
    return SqlTemplate.forUpdate(client, updateTemplate)
      .mapTo(Change::fromRow)
      .execute(params(id, name, version, state, changes))
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
