package io.citadel.context.forum.repository;

import io.citadel.context.forum.Forum;
import io.citadel.context.forum.event.Events;
import io.citadel.eventstore.Data;
import io.citadel.eventstore.data.EventInfo;
import io.citadel.shared.context.Domain;

import java.util.stream.Stream;

public record Sourcing(Forum.ID id) implements Forum.Hydration {
  @Override
  public Forum apply(final long version, final Stream<EventInfo> events) throws Throwable {
    return events
      .map(Forum.event::fromEventInfo)
      .reduce(
        Forum.states.identity(id, version),
        (forum, event) -> switch (event) {
          case Events.Registered registered -> forum.register(registered.name(), registered.description(), registered.by());
          case Events.Opened opened -> forum.open(opened.by());
          case Events.Closed closed -> forum.close(closed.by());
          case Events.Edited.Name edit -> forum.edit(edit.name());
          case Events.Edited.Description edit -> forum.edit(edit.description());
          case Events.Reopened reopened -> forum.reopen(reopened.by());
        },
        (f, __) -> f
      );
  }
}
