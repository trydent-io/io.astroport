package io.citadel.eventstore;

import io.citadel.shared.lang.Array;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.templates.SqlTemplate;

import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;

@SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
record Client(SqlClient client) implements EventStore {
  @Override
  public Future<Stream<EventLog>> findBy(final String aggregateId, final String aggregateName) {
    return SqlTemplate.forQuery(client, """
        with aggregate as (
          select  aggregate_version as version
          from    event_logs
          where   aggregate_id = #{aggregateId} and aggregate_name = #{aggregateName}
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
          "aggregateId", aggregateId,
          "aggregateName", aggregateName
        )
      )
      .map(rows -> stream(rows.spliterator(), false));
  }

  @SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
  @Override
  public Future<Void> persist(EventLog.AggregateInfo aggregate, EventLog.EventInfo... events) {
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
      .execute(
        Map.of(
          "events", Array.of(events).asJsonArray()
        )
      )
      .mapEmpty();
  }
}
