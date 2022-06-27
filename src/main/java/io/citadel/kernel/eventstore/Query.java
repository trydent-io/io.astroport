package io.citadel.kernel.eventstore;

import io.citadel.kernel.eventstore.meta.*;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.templates.SqlTemplate;

import java.util.Map;
import java.util.stream.Stream;

import static io.citadel.kernel.eventstore.meta.Aggregate.id;
import static io.citadel.kernel.eventstore.meta.Aggregate.name;
import static java.util.stream.StreamSupport.stream;

interface Query {
  SqlClient sqlClient();
  default <T> Future<Stream<Aggregate>> aggregates(T id, String name) {
    return aggregates(id(id), name(name));
  }

  default Future<Stream<Aggregate>> aggregates(ID id, Name name) {
    return aggregates(id, name, Version.Last);
  }

  default <T> Future<Stream<Aggregate>> aggregates(T id, String name, long version) {
    return aggregates(id(id), name(name), Aggregate.version(version));
  }

  default Future<Stream<Aggregate>> aggregates(ID id, Name name, Version version) {
    return SqlTemplate.forQuery(sqlClient(), """
        with aggregate as (
          select  aggregate_version as version, aggregate_state as state, aggregate_id as id, aggregate_name as name
          from    metadata
          where   aggregate_id = #{aggregateId}
            and   lower(aggregate_name) = lower(#{aggregateName}) or #{aggregateName} is null
            and   aggregate_version <= #{aggregateVersion} or #{aggregateVersion} = 0
          order by aggregate_version desc
          limit 1
        ), aggregated as (
          select aggregate_id, jsonb_object_agg(json.key, json.value) as model
          from (select aggregate_id, event_data, aggregate_name, aggregate_version from metadata order by timepoint) as meta, jsonb_each(event_data) as json(key, value)
          where   aggregate_id = aggregate.id
            and   aggregate_name = aggregate.name
            and   aggregate_version <= aggregate.version
          group by aggregate_id
        )
        select  id, name, version, state, model
        from    aggregate inner join aggregated
              on aggregate.id = aggregated.aggregate_id
        """)
      .mapTo(row -> aggregate(id, name, row))
      .execute(params(id, name, version))
      .map(rows -> stream(rows.spliterator(), false));
  }

  @SuppressWarnings("DuplicateBranchesInSwitch")
  private Aggregate aggregate(ID id, Name name, Row row) {
    return switch (row.getJsonObject("data")) {
      case null -> Aggregate.identity(id, name);
      case JsonObject it && it.isEmpty() -> Aggregate.identity(id, name);
      default -> Aggregate.entity(
        Aggregate.id(row.getString("id")),
        Aggregate.name(row.getString("name")),
        Aggregate.version(row.getLong("version")),
        Aggregate.state(row.getString("state")),
        Aggregate.model(row.getJsonObject("model"))
      );
    };
  }

  private Map<String, Object> params(ID id, Name name, Version version) {
    return Map.of(
      "aggregateId", id.toString(),
      "aggregateName", name,
      "aggregateVersion", version
    );
  }
}
