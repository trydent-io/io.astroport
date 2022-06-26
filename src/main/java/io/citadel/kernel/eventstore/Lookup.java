package io.citadel.kernel.eventstore;

import io.citadel.kernel.eventstore.meta.*;
import io.citadel.kernel.eventstore.meta.Feed;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.templates.SqlTemplate;

import java.util.Map;
import java.util.stream.StreamSupport;

import static java.util.stream.StreamSupport.stream;

@SuppressWarnings("DuplicateBranchesInSwitch")
final class Lookup implements EventStore {
  private final SqlClient client;

  Lookup(SqlClient client) {
    this.client = client;
  }

  @Override
  public Future<Aggregate> aggregate(final ID id, final Name name, final Version version) {
    return SqlTemplate.forQuery(client, """
        with aggregate as (
          select  aggregate_version as version, aggregate_state as state, aggregate_id as id, aggregate_name as name
          from    metadata
          where   aggregate_id = #{aggregateId}
            and   lower(aggregate_name) = lower(#{aggregateName}) or #{aggregateName} is null
            and   aggregate_version <= #{aggregateVersion} or #{aggregateVersion} = 0
          order by aggregate_version desc
          limit 1
        ), aggregated as (
          select aggregate_id, jsonb_object_agg(json.key, json.value) as data
          from (select aggregate_id, event_data, aggregate_name, aggregate_version from metadata order by timepoint) as meta, jsonb_each(event_data) as json(key, value)
          where   aggregate_id = aggregate.id
            and   aggregate_name = aggregate.name
            and   aggregate_version <= aggregate.version
          group by aggregate_id
        )
        select  id, name, version, state, data
        from    aggregate inner join aggregated
              on aggregate.id = aggregated.aggregate_id
        """)
      .mapTo(row ->
        switch (row.getJsonObject("data")) {
          case null -> Aggregate.identity(id);
          case JsonObject it && it.isEmpty() -> Aggregate.identity(id);
          default -> Aggregate.entity(
            id,
            Aggregate.version(row.getLong("version")),
            Aggregate.state(row.getString("state")),
            Aggregate.data(row.getJsonObject("data"))
          );
        }
      )
      .execute(
        Map.of(
          "aggregateId", id.toString(),
          "aggregateName", name,
          "aggregateVersion", version
        )
      )
      .map(rows -> stream(rows.spliterator(), false))
      .map(stream -> stream.findFirst().orElseGet(() -> Aggregate.identity(id)));
  }

}
