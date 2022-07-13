package io.citadel.kernel.eventstore;

import io.citadel.kernel.eventstore.metadata.MetaAggregate;
import io.citadel.kernel.eventstore.metadata.Change;
import io.citadel.kernel.eventstore.audit.ID;
import io.citadel.kernel.eventstore.audit.Name;
import io.citadel.kernel.eventstore.metadata.State;
import io.citadel.kernel.eventstore.audit.Version;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.templates.SqlTemplate;

import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;

final class Client implements Entities, Query, Update {
  private final EventBus eventBus;
  private final SqlClient client;

  Client(EventBus eventBus, SqlClient client) {
    this.eventBus = eventBus;
    this.client = client;
  }

  @SuppressWarnings("DuplicateBranchesInSwitch")
  @Override
  public Future<MetaAggregate> query(ID id, Name name, Version version) {
    return SqlTemplate.forQuery(client, queryTemplate)
      .mapTo(row -> switch (row.getJsonObject("data")) {
        case null -> MetaAggregate.zero(id, name);
        case JsonObject it && it.isEmpty() -> MetaAggregate.zero(id, name);
        default -> MetaAggregate.last(
          MetaAggregate.id(row.getString("id")),
          MetaAggregate.name(row.getString("name")),
          MetaAggregate.version(row.getLong("version")),
          MetaAggregate.state(row.getString("state")),
          MetaAggregate.model(row.getJsonObject("entity"))
        );
      })
      .execute(params(id, name, version))
      .map(rows -> stream(rows.spliterator(), false))
      .map(Stream::findFirst)
      .map(found -> found.orElseGet(() -> MetaAggregate.zero(id, name)));
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
