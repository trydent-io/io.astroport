package io.citadel.kernel.eventstore;

import io.citadel.kernel.eventstore.meta.Aggregate;
import io.citadel.kernel.eventstore.meta.Event;
import io.citadel.kernel.eventstore.meta.Feed;
import io.citadel.kernel.eventstore.meta.ID;
import io.citadel.kernel.eventstore.meta.Name;
import io.citadel.kernel.eventstore.meta.State;
import io.citadel.kernel.eventstore.meta.Version;
import io.citadel.kernel.media.Json;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.templates.SqlTemplate;

import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;

sealed interface Persist permits EventStore.Client {
  default Future<Void> unit(ID id, Name name, Version version, State state, Stream<Event> changes) {
    return switch (this) {
      case EventStore.Client it -> SqlTemplate.forUpdate(it.client(), """
          with events as (
            select  es -> 'event' ->> 'name' event_name,
                    es -> 'event' ->> 'data' event_data
            from json_array_elements(#{events}) es
          ),
          last_version as (
            select  e.aggregate_version
            from    metadata e
            where   aggregate_id = #{aggregateId}
              and   aggregate_name = #{aggregateName}
            order by e.aggregate_version desc
            limit 1
          )
          insert into metadata(event_name, event_data, aggregate_id, aggregate_name, aggregate_version)
          select  event_name,
                  event_data,
                  #{aggregateId},
                  #{aggregateName},
                  #{aggregateVersion} + 1
          from  metadata
          where #{aggregateVersion} = last_version or (#{aggregateVersion} = 0 and not exists(select id from metadata where aggregate_id = #{aggregateId}))
          returning aggregate_id, aggregate_name, event_name, event_data, timepoint
          """)
        .mapTo(Event::fromRow)
        .execute(
          Map.of(
            "aggregateId", id.value(),
            "aggregateName", name.value(),
            "aggregateVersion", version.value(),
            "aggregateState", state.value(),
            "events", Json.array(changes)
          )
        )
        .map(events -> stream(events.spliterator(), false))
        .onSuccess(events ->
          events.forEach(event ->
            it.eventBus().publish(event.name(), event.data(), new DeliveryOptions()
              .addHeader("aggregateId", event.entity().id().toString())
              .addHeader("timepoint", event.timepoint().asIsoDateTime())
            )
          )
        )
        .mapEmpty();
      ;

    }
  }

  @SuppressWarnings("DuplicateBranchesInSwitch")
  private Aggregate aggregate(ID id, Name name, Row row) {
    return switch (row.getJsonObject("data")) {
      case null -> Aggregate.identity(id, name);
      case JsonObject it && it.isEmpty() -> Aggregate.identity(id, name);
      default -> Aggregate.entity(
        Aggregate.id(row.getString("id")),
        Aggregate.name(row.getString("name")),
        Aggregate.version(row.getLong("version")),
        Aggregate.state(row.getString("state")),
        Aggregate.model(row.getJsonObject("model"))
      );
    };
  }

  private Map<String, Object> params(ID id, Name name, Version version) {
    return Map.of(
      "aggregateId", id.toString(),
      "aggregateName", name,
      "aggregateVersion", version
    );
  }
}
