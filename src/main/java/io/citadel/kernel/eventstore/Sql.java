package io.citadel.kernel.eventstore;

import io.citadel.kernel.eventstore.meta.*;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.templates.SqlTemplate;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

@SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
final class Sql implements Lookup<Meta.Log> {
  private final Vertx vertx;
  private final SqlClient client;

  Sql(Vertx vertx, SqlClient client) {
    this.vertx = vertx;
    this.client = client;
  }

  @Override
  public Future<Meta.Log> find(final ID id, final Name name, final Version version) {
    return SqlTemplate.forQuery(client, """
        with entity as (
          select  entity_version as version
          from    event_store
          where   entity_id = #{entityId}
            and   lower(entity_name) = lower(#{entityName}) or #{entityName} is null
            and   entity_version <= #{entityVersion} or #{entityVersion} = 0
          order by entity_version desc
          limit 1
        )
        select  event_name, event_data, entity_id, entity_name, (select version from entity) as entity_version, timepoint
        from    event_store
        where   entity_id = #{entityId} and (lower(entity_name) = lower(#{entityName}) or #{entityName} is null)
        """)
      .mapTo(row -> row)
      .execute(
        Map.of(
          "entityId", id.toString(),
          "entityName", name,
          "entityVersion", version
        )
      )
      .map(Meta::fromRows);
  }

  private ToFeed toFeedEntity(ID id, Name name, Version version) {
    return new ToFeed(Entity.of(id, name, version));
  }

  private final class ToFeed implements Aggregator<Meta.Log, Feed[], Feed> {
    private Entity entity;
    private Stream<Event> events;
    private ToFeed(Entity entity) {
      this(entity, Stream.empty());
    }
    private ToFeed(Entity entity, Stream<Event> events) {
      this.entity = entity;
      this.events = events;
    }

    @Override
    public Supplier<Feed[]> supplier() {
      return () -> new Feed[]{new Feed(vertx, client, entity)};
    }

    @Override
    public BiConsumer<Feed[], Meta.Log> accumulator() {
      return (snapshots, log) -> snapshots[0] = snapshots[0]
        .entity(log.entity())
        .append(log.event());
    }

    @Override
    public Function<Feed[], Feed> finisher() {
      return snapshots -> snapshots[0];
    }
  }
}
