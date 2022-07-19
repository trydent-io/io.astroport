package io.citadel.kernel.eventstore;

import io.citadel.kernel.eventstore.event.Entity;
import io.citadel.kernel.eventstore.event.ID;
import io.citadel.kernel.eventstore.event.Name;
import io.citadel.kernel.eventstore.event.Version;

import java.util.Map;

import static java.util.stream.StreamSupport.stream;

sealed interface Query permits Client {
  String queryTemplate = """
    with lookup as (
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
      where   aggregate_id = lookup.id
        and   aggregate_name = lookup.name
        and   aggregate_version <= lookup.version
      group by aggregate_id
    )
    select  id, name, version, state, model
    from    lookup inner join aggregated
          on lookup.id = aggregated.aggregate_id
    """;

  default Map<String, Object> params(Entity.ID id, Entity.Name name, Entity.Version version) {
    return Map.of(
      "aggregateId", id.toString(),
      "aggregateName", name,
      "aggregateVersion", version
    );
  }
}
