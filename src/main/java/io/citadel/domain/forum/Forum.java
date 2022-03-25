package io.citadel.domain.forum;

import io.citadel.domain.forum.command.Commands;
import io.citadel.domain.forum.entity.Archived;
import io.citadel.domain.forum.entity.Closed;
import io.citadel.domain.forum.entity.Model;
import io.citadel.domain.forum.entity.Machine;
import io.citadel.domain.forum.entity.Registered;
import io.citadel.domain.forum.event.Events;
import io.citadel.domain.forum.model.Attributes;
import io.citadel.domain.forum.repository.Sourcing;
import io.citadel.domain.forum.state.Operations;
import io.citadel.domain.forum.state.States;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.domain.attribute.Attribute;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public sealed interface Forum extends Operations permits States.Aggregate {
  Commands commands = Commands.Defaults;
  Events event = Events.Defaults;
  Attributes attributes = Attributes.Defaults;
  States states = States.Defaults;

  enum State implements Domain.State<State> {Registered, Open, Closed, Archived}

  sealed interface Command extends Domain.Command permits Commands.Close, Commands.Edit, Commands.Open, Commands.Register, Commands.Reopen {}
  sealed interface Event extends Domain.Event permits Events.Closed, Events.Edited, Events.Opened, Events.Registered, Events.Reopened {}
  sealed interface Hydration extends Domain.Hydration<Forum> permits Sourcing {}

  record ID(UUID value) implements Domain.ID<Forum.ID> {}
  record Name(String value) implements Attribute<String> {}
  record Description(String value) implements Attribute<String> {}

  sealed interface Entity extends Domain.Entity<State> permits Model {
    Optional<Entity> edit(Name name);
    Optional<Entity> edit(Description description);
    Optional<Entity> register(Name name, Description description, LocalDateTime registeredAt);
    Optional<Entity> open(LocalDateTime openedAt);
    Optional<Entity> close(LocalDateTime closedAt);
    Optional<Entity> archive(LocalDateTime archivedAt);
  }
}

