package io.citadel.context.forum;

import io.citadel.eventstore.EventLog;
import io.citadel.eventstore.EventStore;
import io.citadel.shared.domain.Domain;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;

import java.util.stream.Stream;

import static io.citadel.context.forum.Forums.Type.Grouped.grouped;


public sealed interface Forums extends Domain.Repository<Forum.ID, Forum> {
  static Forums from(EventBus eventBus) {
    return new Type.EventSourced(eventBus);
  }

  enum Type {;

    private record Grouped(Domain.Version version, Stream<Forum.Event> events) {
      static Grouped grouped(EventLog.AggregateInfo aggregate, Stream<EventLog.EventInfo> events) {
        return new Grouped(aggregate.version(), events.map(Forum.events::fromInfo));
      }
    }

    private record EventSourced(EventBus eventBus) implements Forums {
      @Override
      public Forum load(Forum.ID id) {
        return eventBus.<Stream<EventLog>>request(EventStore.operations.FIND_BY, id.value().toString())
          .map(Message::body)
          .map(eventLogs -> eventLogs
            .findFirst()
            .map(it -> grouped(it.aggregate(), eventLogs.map(EventLog::event)))
            .orElseThrow()
          )
          .map(it -> Forum.states.from());
      }
    }
  }
}
