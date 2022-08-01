package io.citadel.kernel.eventstore;

import io.citadel.kernel.eventstore.event.Audit;
import io.citadel.kernel.media.Json;

import java.util.Map;
import java.util.stream.Stream;

sealed interface Update permits Client {
  String updateTemplate = """
    with events as (
      select  es -> 'entity' ->> 'id' entity_id,
              es -> 'entity' ->> 'name' entity_name,
              es -> 'entity' ->> 'version' entity_version,
              es -> 'event' ->> 'name' event_name,
              es -> 'event' ->> 'data' event_data
      from json_array_elements(#{events}) es
    )
    insert into entity_events(event_name, event_data, entity_id, entity_name, entity_version)
    select  event_name,
            event_data,
            entity_id,
            entity_name,
            entity_version + 1
    from    events es
    where   es.entity_version = (
              select entity_version
              from entity_events ees
              where ees.entity_id = es.entity_id
                and ees.entity_name = es.entity_name
              order by ees.entity_version
              limit 1
            )
      or    (es.entity_version = 0 and not exists(select ee.event_id from entity_events ee where ee.entity_id = es.entity_id))
    returning event_id, event_name, event_data, event_timepoint
    """;

  default Map<String, Object> with(Stream<Audit> events) {
    return Map.of("events", Json.array(events));
  }

}
