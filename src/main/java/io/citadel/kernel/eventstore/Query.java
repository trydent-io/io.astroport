package io.citadel.kernel.eventstore;

import io.citadel.kernel.eventstore.event.Entity;

import java.util.Map;

sealed interface Query permits Client {
  String modelTemplate = """
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

  String queryTemplate = """
    with last as (
      select  entity_id as id, entity_name as name, entity_version as version
      from    entity_events
      where   entity_id = #{entityId}
        and   lower(entity_name) = lower(#{entityName}) or #{entityName} is null
        and   entity_version <= #{entityVersion} or #{entityVersion} = 0
      order by entity_version desc
      limit 1
    )
    select  event_id, event_name, event_data, event_timepoint, entity_id, entity_name, entity_version
    from    entity_events
    where   entity_id = last.id
      and   entity_name = last.name
      and   entity_version <= last.version
    order by event_timepoint;
    """;

  default Map<String, Object> params(Entity.ID id, Entity.Name name, Entity.Version version) {
    return Map.of(
      "entityId", id.toString(),
      "entityName", name,
      "entityVersion", version
    );
  }
}
