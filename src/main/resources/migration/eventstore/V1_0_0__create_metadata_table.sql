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
