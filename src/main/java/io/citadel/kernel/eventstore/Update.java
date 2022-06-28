package io.citadel.kernel.eventstore;

import io.citadel.kernel.eventstore.metadata.Change;
import io.citadel.kernel.eventstore.metadata.ID;
import io.citadel.kernel.eventstore.metadata.Name;
import io.citadel.kernel.eventstore.metadata.State;
import io.citadel.kernel.eventstore.metadata.Version;
import io.citadel.kernel.media.Json;

import java.util.Map;
import java.util.stream.Stream;

sealed interface Update permits Client {
  String updateTemplate = """
    with events as (
      select  es -> 'change' ->> 'name' event_name,
              es -> 'change' ->> 'data' event_data
      from json_array_elements(#{events}) es
    ),
    last_version as (
      select  e.aggregate_version
      from    metadata e
      where   aggregate_id = #{aggregateId}
        and   aggregate_name = #{aggregateName}
      order by e.aggregate_version desc
      limit 1
    )
    insert into metadata(event_name, event_data, aggregate_id, aggregate_name, aggregate_version)
    select  event_name,
            event_data,
            #{aggregateId},
            #{aggregateName},
            #{aggregateVersion} + 1
    from  metadata
    where #{aggregateVersion} = last_version or (#{aggregateVersion} = 0 and not exists(select id from metadata where aggregate_id = #{aggregateId}))
    returning aggregate_id, aggregate_name, event_name, event_data, timepoint
    """;

  default Map<String, Object> params(ID id, Name name, Version version, State state, Stream<Change> changes) {
    return Map.of(
      "aggregateId", id.value(),
      "aggregateName", name.value(),
      "aggregateVersion", version.value(),
      "aggregateState", state.value(),
      "events", Json.array(changes)
    );
  }

}
