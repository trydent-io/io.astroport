package io.citadel.forum;

import io.citadel.forum.model.Attributes;
import io.citadel.forum.command.Close;
import io.citadel.forum.command.Commands;
import io.citadel.forum.command.Open;
import io.citadel.forum.event.Events;
import io.citadel.forum.event.Opened;
import io.citadel.forum.state.Closed;
import io.citadel.forum.state.Initial;
import io.citadel.forum.state.States;
import io.citadel.kernel.domain.Domain;

import java.util.UUID;
import java.util.stream.Stream;

public sealed interface Forum extends Domain.Aggregate<Forum.Command, Forum.Event, Forum> permits Closed, Initial, io.citadel.forum.state.Opened {
  String name = "Forum";
  Commands commands = Commands.Defaults;
  Events events = Events.Defaults;
  States states = States.Defaults;
  Attributes attributes = Attributes.Defaults;

  static Forum from(ID identity, Stream<Forum.Event> events) {
    return events
      .map(Forum.Event::asCommand)
      .reduce(Forum.states.initial(identity), Forum::apply, (f, f2) -> f2)
      .flush();
  }

  sealed interface Command extends Domain.Command<Forum.Event>
    permits Close, Open {}

  sealed interface Event extends Domain.Event<Forum.Command>
    permits Opened {}

  record ID(UUID get) implements Domain.ID<UUID> {}
}
