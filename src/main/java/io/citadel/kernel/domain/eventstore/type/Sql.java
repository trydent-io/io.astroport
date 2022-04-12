package io.citadel.kernel.domain.eventstore.type;

import io.citadel.kernel.domain.eventstore.EventStore;
import io.citadel.kernel.domain.eventstore.data.AggregateInfo;
import io.citadel.kernel.domain.eventstore.data.EventInfo;
import io.citadel.kernel.domain.eventstore.data.EventLog;
import io.citadel.kernel.domain.eventstore.event.Events;
import io.citadel.kernel.media.Json;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.templates.SqlTemplate;

import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;

@SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
public record Sql(EventBus eventBus, SqlClient client) implements EventStore {
  @Override
  public Future<Events> findEventsBy(String id, String name) {
    return SqlTemplate.forQuery(client, """
        with aggregate as (
          select  aggregate_version as version
          from    event_logs
          where   aggregate_id = #{aggregateId}
            and   aggregate_name = #{aggregateName}
            and   aggregate_version <= #{aggregateVersion}
          order by aggregate_version desc
          limit 1
        )
        select  id, event_name, event_data, aggregate_id, aggregate_name, (select version from aggregate) as aggregate_version, persisted_at
        from    event_logs
        where   aggregate_id = #{aggregateId} and aggregate_name = #{aggregateName}
        """)
      .mapTo(EventLog::fromRow)
      .execute(
        Map.of(
          "aggregateId", id,
          "aggregateName", name
        )
      )
      .map(rows -> stream(rows.spliterator(), false))
      .map(eventLogs -> eventLogs
        .findFirst()
        .map(eventLog ->
          Events.found(
            eventLog.aggregate().id(),
            eventLog.aggregate().version(),
            eventLogs.map(EventLog::event)
          )
        )
        .orElseGet(Events::empty)
      );
  }

  @Override
  public Future<Stream<EventLog>> persist(AggregateInfo aggregate, Stream<EventInfo> events, String by) {
    final var template = """
      with events as (
        select  es -> 'event' ->> 'name' event_name,
                es -> 'event' ->> 'data' event_data
        from json_array_elements(#{events}) es
      ),
      last_version as (
        select  e.aggregate_version
        from    event_logs e
        where   aggregate_id = #{aggregateId}
          and   aggregate_name = #{aggregateName}
        order by e.aggregate_version desc
        limit 1
      )
      insert into event_logs(event_name, event_data, aggregate_id, aggregate_name, aggregate_version)
      select  event_name,
              event_data,
              #{aggregateId},
              #{aggregateName},
              #{aggregateVersion} + 1
      from  events
      where #{aggregateVersion} = last_version or (#{aggregateVersion} = 0 and not exists(select id from event_logs where aggregate_id = #{aggregateId}))
      returning *
      """;
    return SqlTemplate.forUpdate(client, template)
      .mapTo(EventLog::fromRow)
      .execute(
        Map.of(
          "aggregateId", aggregate.id(),
          "aggregateName", aggregate.name(),
          "aggregateVersion", aggregate.version(),
          "events", Json.array(events)
        )
      )
      .map(it -> stream(it.spliterator(), false))
      .onSuccess(eventLogs -> eventLogs
        .forEach(eventLog ->
          eventBus.send(
            eventLog.event().name(),
            eventLog.event().data(),
            new DeliveryOptions()
              .addHeader("persistedAt", eventLog.persistedAt().toString())
              .addHeader("persistedBy", eventLog.persistedBy())
          )
        )
      );
  }
}
