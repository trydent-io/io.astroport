package io.citadel.context.forum;

import io.citadel.eventstore.Entries;
import io.citadel.eventstore.EventStore;
import io.citadel.eventstore.Operations;
import io.citadel.shared.context.Domain;
import io.vertx.core.Future;

public sealed interface Forums extends Domain.Repository<Forum, Forum.ID, Forum.Model> {
  static Forums stored(EventStore eventStore) {
    return new Type.Stored(eventStore);
  }

  enum Type {
    ;

    private record Stored(EventStore eventStore) implements Forums {
      @Override
      public Future<Forum> load(final Forum.ID id) {
        return eventStore.findEventsBy(new Entries.Aggregate(id.value().toString(), "forum"))
          .map(found -> Forum.states. hydrate(found)
          ;
      }

      private Forum.Model hydrate(final Operations.FoundEvents found) {
        return found.events()
          .map(Forum.events::fromFound)
          .reduce(
            new Forum.Model(),
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
  }
}
