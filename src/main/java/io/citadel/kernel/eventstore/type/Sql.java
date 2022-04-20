package io.citadel.eventstore.type;

import io.citadel.CitadelException;
import io.citadel.eventstore.EventStore;
import io.citadel.eventstore.data.Feed;
import io.citadel.kernel.media.Json;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.templates.SqlTemplate;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
public record Sql(EventBus eventBus, SqlClient client) implements EventStore {
  @Override
  public Future<Feed> seek(String id, String name) {
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
      .mapTo(row -> row)
      .execute(
        Map.of(
          "aggregateId", id,
          "aggregateName", name
        )
      )
      .map(Feed::fromRows);
  }

  @Override
  public Future<Feed> persist(Stream<Feed.Entry> entries) {
    return Optional.ofNullable(entries)
      .flatMap(Stream::findFirst)
      .map(entry ->
        SqlTemplate.forUpdate(client, """
          with events as (
            select  es -> 'event' ->> 'name' event_name,
                    es -> 'event' ->> 'data' event_data
            from json_array_elements(#{events}) es
          ),
          last_version as (
            select  e.aggregate_version
            from    feed e
            where   aggregate_id = #{aggregateId}
              and   aggregate_name = #{aggregateName}
            order by e.aggregate_version desc
            limit 1
          )
          insert into feed(event_name, event_data, aggregate_id, aggregate_name, aggregate_version)
          select  event_name,
                  event_data,
                  #{aggregateId},
                  #{aggregateName},
                  #{aggregateVersion} + 1
          from  events
          where #{aggregateVersion} = last_version or (#{aggregateVersion} = 0 and not exists(select id from event_logs where aggregate_id = #{aggregateId}))
          returning *
          """)
          .mapTo(row -> row)
          .execute(
            Map.of(
              "aggregateId", entry.aggregate().id(),
              "aggregateName", entry.aggregate().name(),
              "aggregateVersion", entry.aggregate().version(),
              "events", Json.array(entries.map(Feed.Entry::event))
            )
          )
          .map(Feed::fromRows)
      ).orElseThrow(() -> new CitadelException("Can't persist empty feed entries"));
  }
}
