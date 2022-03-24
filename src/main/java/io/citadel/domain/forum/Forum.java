package io.citadel.domain.forum;

import io.citadel.domain.forum.command.Commands;
import io.citadel.domain.forum.event.Events;
import io.citadel.domain.forum.model.Attributes;
import io.citadel.domain.forum.repository.Sourcing;
import io.citadel.domain.forum.state.Operations;
import io.citadel.domain.forum.state.States;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.domain.attribute.Attribute;

import java.util.UUID;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public sealed interface Forum extends Operations permits States.Aggregate {
  Commands commands = Commands.Defaults;
  Events event = Events.Defaults;
  Attributes attributes = Attributes.Defaults;
  States states = States.Defaults;

  enum State implements Domain.State<State> {Initial, Registered, Open, Closed}

  sealed interface Command extends Domain.Command permits Commands.Close, Commands.Edit, Commands.Open, Commands.Register, Commands.Reopen {}
  sealed interface Event extends Domain.Event permits Events.Closed, Events.Edited, Events.Opened, Events.Registered, Events.Reopened {}
  sealed interface Hydration extends Domain.Hydration<Forum> permits Sourcing {}

  record ID(UUID value) implements Domain.ID<UUID> {}
  record Name(String value) implements Attribute<String> {}
  record Description(String value) implements Attribute<String> {}
}

