package io.citadel.domain.forum;

import io.citadel.domain.forum.aggregate.Model;
import io.citadel.domain.forum.command.Commands;
import io.citadel.domain.forum.event.Events;
import io.citadel.domain.forum.model.Attributes;
import io.citadel.domain.forum.repository.Sourcing;
import io.citadel.domain.forum.state.States;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.domain.attribute.Attribute;
import io.citadel.kernel.func.ThrowableFunction;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.isNull;

public sealed interface Forum extends Domain.Aggregate<Forum>, Operations<Forum> permits States.Aggregate {
  Commands commands = Commands.Defaults;
  Events event = Events.Defaults;
  Attributes attributes = Attributes.Defaults;
  States states = States.Defaults;

  enum State implements Domain.State<State> {Registered, Open, Closed, Archived}

  sealed interface Command extends Domain.Command permits Commands.Close, Commands.Edit, Commands.Open, Commands.Register, Commands.Reopen {}
  sealed interface Event extends Domain.Event permits Events.Archived, Events.Closed, Events.Edited, Events.Opened, Events.Registered, Events.Reopened {}
  sealed interface Hydration extends Domain.Hydration<Forum> permits Sourcing {}

  record ID(UUID value) implements Domain.ID<Forum.ID> {}
  record Name(String value) implements Attribute<String> {}
  record Description(String value) implements Attribute<String> {}

  record Entity(Forum.Name name, Forum.Description description) implements Domain.Entity<Forum.Entity, Forum.Event> {
    @Override
    public Optional<Entity> apply(final Event event) {
      return Optional.empty();
    }
  }
}

