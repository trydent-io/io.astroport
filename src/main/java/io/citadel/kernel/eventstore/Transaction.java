package io.citadel.kernel.eventstore;

import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.media.JsonMedia;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.templates.SqlTemplate;

import java.util.Map;
import java.util.stream.Stream;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

sealed interface Transaction {
  static Transaction open(Vertx vertx, SqlClient sqlClient) {
    return new Open(vertx, sqlClient);
  }
  Transaction log(Domain.Event event);
  Future<Void> commit(Domain.ID<?> aggregateId, String aggregateName, long aggregateVersion);

  @SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
  final class Open implements Transaction {
    private final EventBus eventBus;
    private final SqlClient sqlClient;
    private final Stream<Domain.Event> changes;

    private Open(Vertx vertx, SqlClient sqlClient) {
      this(vertx.eventBus(), sqlClient, Stream.empty());
    }
    private Open(EventBus eventBus, SqlClient sqlClient, Stream<Domain.Event> changes) {
      this.eventBus = eventBus;
      this.sqlClient = sqlClient;
      this.changes = changes;
    }

    private Stream<Domain.Event> append(Domain.Event event) {
      return Stream.concat(this.changes, Stream.of(event));
    }

    @Override
    public Transaction log(Domain.Event event) {
      return new Open(eventBus, sqlClient, append(event));
    }

    @Override
    public Future<Void> commit(Domain.ID<?> aggregateId, String aggregateName, long aggregateVersion) {
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
            "aggregateId", aggregateId.toString(),
            "aggregateName", aggregateName,
            "aggregateVersion", aggregateVersion,
            "events", JsonMedia.array(changes)
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
        )
        .mapEmpty();
    }
  }
}
