create table if not exists metadata
(
  id             uuid                     default gen_random_uuid(),
  event_name     text  not null,
  event_data     jsonb not null,
  entity_id      text  not null,
  entity_name    text  not null,
  entity_version integer                  default 0,
  timepoint      timestamp with time zone default (current_timestamp at time zone 'utc'),
  primary key (id)
);

create table if not exists entity
(
  id   text not null,
  name text not null,
  primary key (id),
  unique (id, name)
);

create table if not exists aggregate(
  entity_id text not null,
  version
)
