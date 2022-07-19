create table if not exists entity_events
(
  event_id        uuid                     default gen_random_uuid(),
  event_name      text  not null,
  event_data      jsonb not null,
  event_timepoint timestamp with time zone default (current_timestamp at time zone 'utc'),
  entity_id       text  not null,
  entity_name     text  not null,
  entity_version  integer                  default 0,
  primary key (event_id)
);
