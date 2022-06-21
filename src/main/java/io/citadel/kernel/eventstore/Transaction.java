package io.citadel.kernel.eventstore;

import io.citadel.kernel.eventstore.meta.Entity;
import io.citadel.kernel.eventstore.meta.Event;
import io.citadel.kernel.eventstore.meta.Feed;
import io.citadel.kernel.media.Json;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.templates.SqlTemplate;

import java.util.Map;
import java.util.stream.Stream;

sealed interface Transaction {
  static Transaction open(Vertx vertx, SqlClient sqlClient, Entity entity) {
    return new Open(vertx.eventBus(), sqlClient, entity, Stream.empty());
  }

  Transaction log(Event event);

  default <R extends Record> Transaction log(R event) {
    return log(new Event(event.getClass().getSimpleName(), Json.fromAny(event)));
  }
  Future<Void> commit();

  @SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
  final class Open implements Transaction {
    private final EventBus eventBus;
    private final SqlClient sqlClient;
    private final Entity entity;
    private final Stream<Event> changes;

    public Open(EventBus eventBus, SqlClient sqlClient, Entity entity, Stream<Event> changes) {
      this.eventBus = eventBus;
      this.sqlClient = sqlClient;
      this.entity = entity;
      this.changes = changes;
    }


    private Stream<Event> append(Event event) {
      return Stream.concat(this.changes, Stream.of(event));
    }

    @Override
    public Transaction log(Event event) {
      return new Open(eventBus, sqlClient, entity, append(event));
    }

    @Override
    public Future<Void> commit() {
      return SqlTemplate.forUpdate(sqlClient, """
          with events as (
            select  es -> 'event' ->> 'name' event_name,
                    es -> 'event' ->> 'data' event_data
            from json_array_elements(#{events}) es
          ),
          last_version as (
            select  e.aggregate_version
            from    event_store e
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
            "aggregateId", entity.id().toString(),
            "aggregateName", entity.name().value(),
            "aggregateVersion", entity.version().value(),
            "events", Json.array(changes)
          )
        )
        .map(Feed::fromRows)
        .onSuccess(feed ->
          feed.forEach(entry ->
            eventBus.publish(entry.event().name(), entry.event().data(), new DeliveryOptions()
              .addHeader("aggregateId", entry.entity().id().toString())
              .addHeader("timepoint", entry.timepoint().asIsoDateTime())
            )
          )
        )
        .mapEmpty();
    }
  }
}
