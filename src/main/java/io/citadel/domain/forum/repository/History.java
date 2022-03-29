package io.citadel.domain.forum.repository;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.forum.event.Events;
import io.citadel.eventstore.data.MetaEvent;

import java.util.Optional;
import java.util.stream.Stream;

public record History(Forum.ID id) implements Forum.Hydration {
  @Override
  public Forum.Snapshot snapshot(final long version, final Stream<MetaEvent> events) {
    return events
      .map(Forum.event::fromMeta)
      .reduce(
        Forum.defaults.snapshot(id),
        (forum, event) -> switch (event) {
          case Events.Registered registered -> forum.register(registered.name(), registered.description());
          case Events.Opened opened -> forum.open();
          case Events.Closed closed -> forum.close();
          case Events.Edited edited -> forum.edit(edited.name(), edited.description());
          case Events.Reopened reopened -> forum.reopen();
          case Events.Archived archived -> forum.archive();
        },
        (f, __) -> f
      )
      .asSnapshot();
  }
}
