package io.citadel.context.forum.repository;

import io.citadel.context.forum.Events;
import io.citadel.context.forum.Forum;
import io.citadel.eventstore.Types;
import io.citadel.shared.context.Domain;

import java.util.stream.Stream;

public record Sourcing(Forum.ID id) implements Forum.Hydration {
  @Override
  public Forum tryApply(Domain.Version version, Stream<Types.EventInfo> events) {
    return events
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
        (f, __) -> f);
  }
}
