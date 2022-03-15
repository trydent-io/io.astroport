package io.citadel.context.forum;

import io.citadel.context.forum.command.Commands;
import io.citadel.context.forum.event.Events;
import io.citadel.context.forum.model.Attributes;
import io.citadel.context.forum.repository.Sourcing;
import io.citadel.context.forum.state.Closeable;
import io.citadel.context.forum.state.Editable;
import io.citadel.context.forum.state.Openable;
import io.citadel.context.forum.state.Registerable;
import io.citadel.context.forum.state.States;
import io.citadel.shared.context.Domain;
import io.citadel.shared.context.attribute.Attribute;

import java.util.UUID;
import java.util.function.UnaryOperator;

public sealed interface Forum extends Registerable, Openable, Editable, Closeable permits States.Aggregate {
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

  record Model(Name name, Description description) implements Domain.Model {
    public Model() { this(null, null); }
  }

  @Override
  default boolean is(State state) {
    return switch (this) {
      case States.Aggregate aggregate -> aggregate.root().is(state);
    };
  }

  @Override
  default Domain.Aggregate<Forum, Model, State> nextIf(State state, State next, UnaryOperator<Model> model) {
    return switch (this) { case States.Aggregate aggregate -> aggregate.root().nextIf(state, next, model); };
  }
}

