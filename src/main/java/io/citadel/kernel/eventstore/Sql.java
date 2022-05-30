package io.citadel.kernel.eventstore;

import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.media.Json;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.templates.SqlTemplate;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static io.citadel.kernel.eventstore.Timeline.ToTimeline.toTimelineOf;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;

@SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
public record Sql(EventBus eventBus, SqlClient client) implements EventStore {
  @Override
  public Future<Timeline> find(Meta.Aggregate aggregate) {
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
      .map(Meta::fromRows)
      .map(Meta::stream)
      .map(logs -> logs.collect(toTimelineOf(aggregate)));
  }

  @Override
  public Future<Meta> feed(Meta.Aggregate aggregate, Stream<Meta.Event> events, String by) {
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
      .map(Meta::fromRows)
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

  @Override
  public <ID extends Domain.ID<?>> Future<Timeline<ID>> findTimeline(final ID aggregateId, final String aggregateName, final long aggregateVersion) {
    return SqlTemplate.forQuery(client, """
        with aggregate as (
          select  aggregate_version as version
          from    event_store
          where   aggregate_id = #{aggregateId}
            and   lower(aggregate_name) = lower(#{aggregateName}) or #{aggregateName} is null
            and   aggregate_version <= #{aggregateVersion} or #{aggregateVersion} = -1
          order by aggregate_version desc
          limit 1
        )
        select  id, event_name, event_data, aggregate_id, aggregate_name, (select version from aggregate) as aggregate_version, persisted_at
        from    event_store
        where   aggregate_id = #{aggregateId} and (lower(aggregate_name) = lower(#{aggregateName}) or #{aggregateName} is null)
        """)
      .mapTo(row -> row)
      .execute(
        Map.of(
          "aggregateId", aggregateId.toString(),
          "aggregateName", aggregateName,
          "aggregateVersion", aggregateVersion
        )
      )
      .map(Meta::fromRows)
      .map(Meta::stream)
      .map(logs -> logs.collect(toTimeline(aggregateId, aggregateName, aggregateVersion)));
  }

  private <ID extends Domain.ID<?>> ToTimeline<ID> toTimeline(ID aggregateId, String aggregateName, long aggregateVersion) {
    return new ToTimeline<>(aggregateId, aggregateName, aggregateVersion);
  }

  @SuppressWarnings({"unchecked", "ConstantConditions"})
  public static final class ToTimeline<ID extends Domain.ID<?>> implements Collector<Meta.Log, Timeline<ID>[], Timeline<ID>> {
    private static final Set<Characteristics> IdentityFinish = Set.of(IDENTITY_FINISH);
    private final ID aggregateId;
    private final String aggregateName;
    private final long aggregateVersion;

    public ToTimeline(final ID aggregateId, final String aggregateName, long aggregateVersion) {
      this.aggregateId = aggregateId;
      this.aggregateName = aggregateName;
      this.aggregateVersion = aggregateVersion;
    }

    private Stream<Meta.Event> append(Stream<Meta.Event> events, Meta.Event event) {
      return Stream.concat(events, Stream.of(event));
    }

    @Override
    public Supplier<Timeline<ID>[]> supplier() {
      return () -> (Timeline<ID>[]) new Object[]{new Timeline<>(aggregateId, aggregateName, aggregateVersion)};
    }

    @Override
    public BiConsumer<Timeline<ID>[], Meta.Log> accumulator() {
      return (timelines, log) -> timelines[0] = timelines[0].aggregate(log.aggregate()).append(log.event());
    }

    @Override
    public BinaryOperator<Timeline<ID>[]> combiner() {
      return (timelines, timelines2) -> timelines;
    }

    @Override
    public Function<Timeline<ID>[], Timeline<ID>> finisher() {
      return timelines -> timelines[0];
    }

    @Override
    public Set<Characteristics> characteristics() {
      return IdentityFinish;
    }
  }
}
