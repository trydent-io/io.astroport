package io.citadel.context.forum.repository;

import io.citadel.context.forum.Events;
import io.citadel.context.forum.Forum;
import io.citadel.context.forum.Forums;
import io.citadel.context.forum.Model;
import io.citadel.eventstore.EventStore;
import io.vertx.core.Future;

public record Repository(EventStore eventStore, Forum.Hydration hydration) implements Forums {
  @Override
  public Future<Forum> load(final Forum.ID id) {
    return eventStore.findEventsBy(id.value().toString(), "forum")
      .compose(events -> events
        .aggregate(hydration)
        .map(Future::succeededFuture)
        .or("Can't hydrate aggregate Forum", (t, m) -> Future.failedFuture(new IllegalStateException(t, m)))
      )
      .map(it -> it.flatMap(i -> i))
      ;
  }

  private Model hydrate(final Operations.FoundEvents found) {
    return found.events()
      .map(Forum.events::fromFound)
      .reduce(
        new Model(),
        (model, event) -> switch (event) {
          case Events.Registered registered -> model.registered(registered.by());
          case Events.Reopened reopened -> model.reopened(reopened.by());
          case Events.Closed closed -> model.closed(closed.by());
          case Events.Edited.Name name -> model.name(name.name());
          case Events.Edited.Description description -> model.description(description.description());
          case Events.Opened opened -> model.opened(opened.by());
        },
        (f, f2) -> f
      );
  }
}
