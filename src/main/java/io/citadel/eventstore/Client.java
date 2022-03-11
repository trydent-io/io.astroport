package io.citadel.eventstore;

import io.citadel.eventstore.Entries.Aggregate;
import io.citadel.eventstore.Entries.Event;
import io.citadel.eventstore.Entries.EventLog;
import io.citadel.eventstore.Operations.FoundEvents;
import io.citadel.shared.media.Json;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.templates.SqlTemplate;

import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;

@SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
record Client(SqlClient client) implements EventStore {
  @Override
  public Future<FoundEvents> findEventsBy(final Aggregate aggregate) {
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
      .mapTo(EventStore.entries::storedEvent)
      .execute(
        Map.of(
          "aggregateId", aggregate.id(),
          "aggregateName", aggregate.name(),
          "aggregateVersion", aggregate.version()
        )
      )
      .map(rows -> stream(rows.spliterator(), false))
      .map(entries -> entries
        .findFirst()
        .map(stored ->
          new FoundEvents(
            stored.aggregate().version(),
            entries.map(EventLog::event)
          )
        )
        .orElseGet(() -> new FoundEvents(0, Stream.empty()))
      );
  }

  @Override
  public Future<Stream<EventLog>> persist(Aggregate aggregate, Stream<Event> events) {
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
      .mapTo(EventStore.entries::storedEvent)
      .execute(
        Map.of(
          "aggregateId", aggregate.id(),
          "aggregateName", aggregate.name(),
          "aggregateVersion", aggregate.version(),
          "events", Json.array(events)
        )
      )
      .map(it -> stream(it.spliterator(), false));
  }
}
