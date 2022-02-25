package io.citadel.forum;

import io.citadel.forum.model.Attributes;
import io.citadel.forum.state.Closeable;
import io.citadel.forum.state.Editable;
import io.citadel.forum.state.Openable;
import io.citadel.forum.state.Registerable;
import io.citadel.forum.state.States;
import io.citadel.kernel.domain.Domain;

import java.util.UUID;

public sealed interface Forum extends Registerable, Openable, Editable, Closeable permits States.Closed, States.Initial, States.Open, States.Registered {
  Commands commands = Commands.Defaults;
  Events events = Events.Defaults;
  Attributes attributes = Attributes.Defaults;
  States states = States.Defaults;

  sealed interface Command extends Domain.Command permits Commands.Close, Commands.Edit, Commands.Open, Commands.Register, Commands.Reopen {}

  sealed interface Event extends Domain.Event permits Events.Closed, Events.Edited, Events.Opened, Events.Registered, Events.Reopened {}

  record ID(UUID value) implements Domain.ID<UUID> {}
  record Name(String value) implements Domain.Attribute<String> {}
  record Description(String value) implements Domain.Attribute<String> {}
}

