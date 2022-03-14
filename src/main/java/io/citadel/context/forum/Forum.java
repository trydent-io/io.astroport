package io.citadel.context.forum;

import io.citadel.context.forum.model.Attributes;
import io.citadel.context.forum.state.Closeable;
import io.citadel.context.forum.state.Editable;
import io.citadel.context.forum.state.Openable;
import io.citadel.context.forum.state.Registerable;
import io.citadel.context.forum.state.States;
import io.citadel.context.member.Member;
import io.citadel.shared.context.Domain;

import java.util.UUID;

public sealed interface Forum extends Registerable, Openable, Editable, Closeable permits States.Aggregate, States.EventSourced {
  Commands commands = Commands.Defaults;
  Events events = Events.Defaults;
  Attributes attributes = Attributes.Defaults;
  States states = States.Defaults;

  enum State implements Domain.State<State> {Initial, Registered, Open, Closed}

  sealed interface Command extends Domain.Command permits Commands.Close, Commands.Edit, Commands.Open, Commands.Register, Commands.Reopen {}

  sealed interface Event extends Domain.Event permits Events.Closed, Events.Edited, Events.Opened, Events.Registered, Events.Reopened {}

  sealed interface Hydration extends Domain.Hydration<Forum> {}

  record ID(UUID value) implements Domain.ID<UUID> {}
  record Name(String value) implements Domain.Attribute<String> {}
  record Description(String value) implements Domain.Attribute<String> {}

}

