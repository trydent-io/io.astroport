package io.citadel.domain.forum;

import io.citadel.domain.forum.aggregate.Aggregate;
import io.citadel.domain.forum.aggregate.Defaults;
import io.citadel.domain.forum.aggregate.Lifespan;
import io.citadel.domain.forum.aggregate.Service;
import io.citadel.domain.forum.aggregate.Model;
import io.citadel.domain.forum.command.Commands;
import io.citadel.domain.forum.event.Events;
import io.citadel.domain.forum.model.Attributes;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.domain.attribute.Attribute;

import java.util.UUID;

public sealed interface Forum<F extends Forum<?>> extends Domain.Model permits Aggregate, Lifespan, Model, Service {
  String AGGREGATE_NAME = "FORUM";

  Commands commands = Commands.Companion;
  Events event = Events.Companion;
  Attributes attributes = Attributes.Companion;
  Defaults defaults = Defaults.Companion;

  enum State implements Domain.State<State> {Registered, Open, Closed, Archived}

  sealed interface Command extends Domain.Command permits Commands.Close, Commands.Edit, Commands.Open, Commands.Register, Commands.Reopen {}
  sealed interface Event extends Domain.Event permits Events.Archived, Events.Closed, Events.Edited, Events.Opened, Events.Registered, Events.Reopened {}

  record ID(UUID value) implements Domain.ID<UUID> {}
  record Name(String value) implements Attribute<String> {}
  record Description(String value) implements Attribute<String> {}
  record Details(Name name, Description description) {}

  F register(Forum.Name name, Forum.Description description);
  F edit(Forum.Name name, Forum.Description description);
  F open();
  F close();
  F archive();
  F reopen();

}

