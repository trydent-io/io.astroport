package io.citadel.eventstore;

import io.citadel.shared.media.Array;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.templates.SqlTemplate;

import javax.sql.DataSource;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;

final class Sql implements EventStore {
  private final SqlClient client;
  private final DataSource dataSource;

  Sql(final SqlClient client, final DataSource dataSource) {
    this.client = client;
    this.dataSource = dataSource;
  }

  @Override
  public Future<Stream<EventLog>> findBy(final String aggregateId, final String aggregateName) {
    return SqlTemplate.forQuery(client, """
        select  id, event_name, event_data, aggregate_id, aggregate_name, aggregate_version, persisted_at 
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
      with entries as (
        select  entries -> 'event' ->> 'name' event_name,
                entries -> 'event' ->> 'data' event_data
        from json_array_elements(#{events}) entries
      )
      insert into event_logs(event_name, event_data, aggregate_id, aggregate_name, aggregate_version)
      select  event_name,
              event_data,
              #{aggregateId},
              #{aggregateName},
              #{aggregateVersion} + 1
      from entries
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
