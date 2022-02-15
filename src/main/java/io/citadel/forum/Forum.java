package io.citadel.forum;

import io.citadel.domain.message.Command;
import io.citadel.domain.message.Event;
import io.citadel.forum.command.Commands;
import io.citadel.forum.event.Events;
import io.citadel.forum.state.States;
import io.vertx.core.eventbus.EventBus;

import java.util.function.Function;
import java.util.stream.Stream;

public interface Forum extends Function<Command, Forum> {
  Commands commands = Commands.Defaults;
  Events events = Events.Defaults;
  States states = States.Defaults;

  static Forum from(EventBus eventBus, Stream<Event> events) {
    return events
      .map(Event::asCommand)
      .reduce(Forum.states.initial(eventBus), Forum::apply, (f, f2) -> f2);
  }
}
