create table if not exists metadata
(
  id                uuid                     default gen_random_uuid(),
  event_name        text  not null,
  event_data        jsonb not null,
  aggregate_id      text  not null,
  aggregate_name    text  not null,
  aggregate_version integer                  default 0,
  aggregate_state   text,
  timepoint         timestamp with time zone default (current_timestamp at time zone 'utc'),
  primary key (id),
  unique (event_name, aggregate_id, aggregate_name, aggregate_version)
);
