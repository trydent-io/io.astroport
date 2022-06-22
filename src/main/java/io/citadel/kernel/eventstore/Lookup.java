package io.citadel.kernel.eventstore;

import io.citadel.kernel.eventstore.meta.*;
import io.citadel.kernel.eventstore.meta.Feed;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.templates.SqlTemplate;

import java.util.Map;

final class Lookup implements Metadata {
  private final SqlClient client;

  Lookup(SqlClient client) {
    this.client = client;
  }

  @Override
  public Future<Entity> findEntity(final ID id, final Name name, final Version version) {
    return SqlTemplate.forQuery(client, """
        with entity as (
          select  entity_version as version, entity_state as state, entity_id as id
          from    metadata
          where   entity_id = #{entityId}
            and   lower(entity_name) = lower(#{entityName}) or #{entityName} is null
            and   entity_version <= #{entityVersion} or #{entityVersion} = 0
          order by entity_version desc
          limit 1
        ), aggregated as (
          select jsonb_object_agg(json.key, json.value) as data
          from metadata, jsonb_each(event_data) as json(key, value)
          where   entity_id = entity.id
            and   entity_name = entity.name
            and   entity_version <= entity.version
          order by timepoint
        )
        select  version as entity_version,
                state as entity_state,
                data as entity_data
        from    entity, aggregated
        """)
      .mapTo(row ->
        Entity.found(
          client,
          Entity.id(row.getString("entity_id")),
          Entity.name(row.getString("entity_name")),
          Entity.version(row.getLong("entity_version")),
          Entity.state(row.getString("entity_state")),
          Entity.data(row.getJsonObject("entity_data"))
        )
      )
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
