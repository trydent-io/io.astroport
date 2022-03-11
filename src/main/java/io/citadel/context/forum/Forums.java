package io.citadel.context.forum;

import io.citadel.eventstore.Entries;
import io.citadel.eventstore.EventStore;
import io.citadel.eventstore.Operations;
import io.citadel.shared.context.Domain;
import io.vertx.core.Future;

import java.util.stream.Stream;

public sealed interface Forums extends Domain.Repository<Forum, Forum.ID, Forum.Model> {
  static Forums stored(EventStore eventStore) {
    return new Type.Stored(eventStore);
  }

  enum Type {
    ;

    private record GroupedBy(Domain.Version version, Stream<Forum.Event> events) {
      GroupedBy(long version, Stream<Forum.Event> events) {
        this(Domain.Version.of(version).orElseThrow(), events);
      }
      GroupedBy() {
        this(Domain.Version.zero(), Stream.empty());
      }
    }

    private record Stored(EventStore eventStore) implements Forums {
      @Override
      public Future<Forum> load(final Forum.ID id) {
        return eventStore.findEventsBy(new Entries.Aggregate(id.value().toString(), "forum"))
          .map(eventLogs ->  eventLogs
            .findFirst()
            .map(eventLog ->
              new GroupedBy(
                eventLog.aggregate().version(),
                eventLogs.map(it -> Forum.events.from(it.event()))
              )
            )
            .orElse(new GroupedBy())
          )
          .map(it -> it.isPresent() ? );
      }

      private Forum.Model hydrate(final Operations.FoundEvents found) {
        return found.events()
          .map(Forum.events::from)
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
