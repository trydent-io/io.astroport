package io.citadel.kernel.eventstore;

import io.citadel.kernel.media.Json;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.templates.SqlTemplate;

import java.util.Map;
import java.util.stream.Stream;

import static io.citadel.kernel.eventstore.Past.toTimelineOf;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

@SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
public record Sql(EventBus eventBus, SqlClient client) implements EventStore {
  @Override
  public Future<Timeline> seek(Feed.Aggregate aggregate) {
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
          "aggregateId", aggregate.id(),
          "aggregateName", aggregate.name(),
          "aggregateVersion", aggregate.version()
        )
      )
      .map(Feed::fromRows)
      .map(Feed::stream)
      .map(logs -> logs.collect(toTimelineOf(aggregate)));
  }

  @Override
  public Future<Feed> feed(Feed.Aggregate aggregate, Stream<Feed.Event> events, String by) {
    return SqlTemplate.forUpdate(client, """
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
          "aggregateId", aggregate.id(),
          "aggregateName", aggregate.name(),
          "aggregateVersion", aggregate.version(),
          "events", Json.array(events)
        )
      )
      .map(Feed::fromRows)
      .onSuccess(feed ->
        feed.forEach(entry ->
          eventBus.publish(entry.event().name(), entry.event().data(), new DeliveryOptions()
            .addHeader("aggregateId", entry.aggregate().id())
            .addHeader("persistedAt", entry.persisted().at().format(ISO_DATE_TIME))
            .addHeader("persistedBy", entry.persisted().by())
          )
        )
      );
  }
}
