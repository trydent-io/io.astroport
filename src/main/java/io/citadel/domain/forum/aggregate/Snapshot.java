package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.forum.aggregate.Aggregate;
import io.citadel.domain.forum.aggregate.Service;
import io.citadel.domain.forum.event.Events;
import io.citadel.eventstore.data.EventInfo;
import io.citadel.kernel.domain.Domain;

import java.util.stream.Stream;

record Snapshot(Forum.ID id) implements Domain.Snapshot<Aggregate> {
  @Override
  public Aggregate aggregate(final long version, final Stream<EventInfo> events) {
    return events
      .map(Forum.event::fromInfo)
      .reduce(
        Forum.defaults.model(id),
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
      .eventually(model -> Forum.defaults.aggregate(model, version));
  }
}
