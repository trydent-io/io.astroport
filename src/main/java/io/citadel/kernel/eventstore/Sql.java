package io.citadel.kernel.eventstore;

import io.citadel.kernel.eventstore.meta.*;
import io.citadel.kernel.eventstore.meta.Feed;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.templates.SqlTemplate;

import java.util.Map;

@SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
final class Sql implements Lookup<Feed> {
  private final SqlClient client;

  Sql(SqlClient client) {
    this.client = client;
  }

  @Override
  public Future<Feed> find(final ID id, final Name name, final Version version) {
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
        select  id, event_name, event_data, entity_id, entity_name, (select version from entity) as entity_version, timepoint
        from    event_store
        where   entity_id = #{entityId} and (lower(entity_name) = lower(#{entityName}) or #{entityName} is null)
        """)
      .mapTo(Feed.Log::fromRow)
      .execute(
        Map.of(
          "entityId", id.toString(),
          "entityName", name,
          "entityVersion", version
        )
      )
      .map(Feed::fromRows);
  }

}
