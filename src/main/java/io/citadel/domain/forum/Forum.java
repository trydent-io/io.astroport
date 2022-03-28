package io.citadel.domain.forum;

import io.citadel.domain.forum.command.Commands;
import io.citadel.domain.forum.event.Events;
import io.citadel.domain.forum.model.Attributes;
import io.citadel.domain.forum.repository.Sourcing;
import io.citadel.domain.forum.state.States;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.domain.attribute.Attribute;

import java.time.LocalDateTime;
import java.util.UUID;

public sealed interface Forum extends Domain.Aggregate<Forum> permits States.Aggregate {
  Commands commands = Commands.Defaults;
  Events event = Events.Defaults;
  Attributes attributes = Attributes.Defaults;
  States states = States.Defaults;

  static Forum with(Forum.ID id) { return new States.Aggregate(id); }

  enum State implements Domain.State<State> {Registered, Open, Closed, Archived}

  sealed interface Command extends Domain.Command permits Commands.Close, Commands.Edit, Commands.Open, Commands.Register, Commands.Reopen {}
  sealed interface Event extends Domain.Event permits Events.Archived, Events.Closed, Events.Edited, Events.Opened, Events.Registered, Events.Reopened {}
  sealed interface Hydration extends Domain.Hydration<Forum> permits Sourcing {}

  record ID(UUID value) implements Domain.ID<Forum.ID> {}
  record Name(String value) implements Attribute<String> {}
  record Description(String value) implements Attribute<String> {}
  record Details(Name name, Description description) implements Domain.ValueObject<Details>

  Registered register(Forum.Name name, Forum.Description description);
  interface Registered {
    Open open();
  }
  interface Open {
    Closed close();
  }
  interface Closed {
    Open reopen();
    Archived archive();
  }

  interface Archived {}
}

