create table if not exists metadata (
  id             uuid                     default gen_random_uuid(),
  event_name     text  not null,
  event_data     jsonb not null,
  entity_id      text  not null,
  entity_name    text  not null,
  entity_version integer                  default 0,
  entity_state   text,
  timepoint      timestamp with time zone default (current_timestamp at time zone 'utc'),
  primary key (id),
  unique (event_name, entity_id, entity_name, entity_version)
);
/*
with entity_events as (select ent.id, data
                       from entities ent
                              left join events evt on ent.id = evt.entity_id
                       where ent.identification = ''
                         and ent.name = ''
                       order by evt.timepoint),
     aggregate as (select ee.id, jsonb_object_agg(datum.key, datum.value)
                   from entity_events ee,
                        jsonb_each(ee.data) as datum(key, value)
                   group by ee.id)
select identification as entity_id, name as entity_name, enti, entity_data
from entities
       left join versions aggregate on entities.id = aggregate.id
*/
