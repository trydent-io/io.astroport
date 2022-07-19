package io.citadel.kernel.eventstore;

import io.citadel.kernel.eventstore.event.Entity;
import io.citadel.kernel.eventstore.event.Event;
import io.citadel.kernel.media.Json;

import java.util.Map;
import java.util.stream.Stream;

sealed interface Update permits Client {
  String updateTemplate = """
    with events as (
      select  es -> 'event' ->> 'name' event_name,
              es -> 'event' ->> 'data' event_data
      from json_array_elements(#{events}) es
    ),
    last_version as (
      select  e.entity_version
      from    entity_events e
      where   entity_id = #{entityId}
        and   entity_name = #{entityName}
      order by e.entity_version desc
      limit 1
    )
    insert into entity_events(event_name, event_data, entity_id, entity_name, entity_version)
    select  event_name,
            event_data,
            #{entityId},
            #{entityName},
            #{entityVersion} + 1
    from  entity_events
    where #{entityVersion} = last_version or (#{entityVersion} = 0 and not exists(select event_id from entity_events where entity_id = #{entityId}))
    returning event_id, event_name, event_data, event_timepoint
    """;

  default Map<String, Object> params(Entity.ID id, Entity.Name name, Entity.Version version, Stream<Event> events) {
    return Map.of(
      "entityId", id.value(),
      "entityName", name.value(),
      "entityVersion", version.value(),
      "events", Json.array(events)
    );
  }

}
