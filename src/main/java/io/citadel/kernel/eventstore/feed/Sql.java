package io.citadel.kernel.eventstore.feed;

import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.eventstore.Feed;
import io.citadel.kernel.eventstore.Meta;
import io.citadel.kernel.media.JsonMedia;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.templates.SqlTemplate;

import java.util.Map;
import java.util.stream.Stream;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

@SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
public record Sql(EventBus eventBus, SqlClient client) implements Feed {
  @Override
  public <ID extends Domain.ID<?>> Future<Meta> log(ID aggregateId, String aggregateName, long aggregateVersion, Stream<Feed.Event> events, String by) {
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
        insert into event_store(event_name, event_data, aggregate_id, aggregate_name, aggregate_version)
        select  event_name,
                event_data,
                #{aggregateId},
                #{aggregateName},
                #{aggregateVersion} + 1
        from  event_store
        where #{aggregateVersion} = last_version or (#{aggregateVersion} = 0 and not exists(select id from event_store where aggregate_id = #{aggregateId}))
        returning *
        """)
      .mapTo(row -> row)
      .execute(
        Map.of(
          "aggregateId", aggregateId.toString(),
          "aggregateName", aggregateName,
          "aggregateVersion", aggregateVersion,
          "events", JsonMedia.array(events)
        )
      )
      .map(Meta::fromRows)
      .onSuccess(feed ->
        feed.forEach(entry ->
          eventBus.publish(entry.event().name(), entry.event().data(), new DeliveryOptions()
            .addHeader("aggregateId", entry.aggregate().id().toString())
            .addHeader("persistedAt", entry.event().timepoint().val().format(ISO_DATE_TIME))
            .addHeader("persistedBy", entry.persisted().by())
          )
        )
      );
  }
}
