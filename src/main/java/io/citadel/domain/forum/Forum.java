package io.citadel.domain.forum;

import io.citadel.domain.forum.aggregate.Aggregate;
import io.citadel.domain.forum.aggregate.Defaults;
import io.citadel.domain.forum.aggregate.Lifespan;
import io.citadel.domain.forum.aggregate.Lifecycle;
import io.citadel.domain.forum.aggregate.Snapshot;
import io.citadel.domain.forum.command.Commands;
import io.citadel.domain.forum.event.Events;
import io.citadel.domain.forum.model.Attributes;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.domain.attribute.Attribute;

import java.util.UUID;

public sealed interface Forum<F extends Forum<F>> permits Aggregate, Lifespan, Lifecycle, Snapshot {
  String AGGREGATE_NAME = "FORUM";

  Commands commands = Commands.Companion;
  Events event = Events.Companion;
  Attributes attributes = Attributes.Companion;
  Defaults defaults = Defaults.Companion;

  enum State implements Domain.State<State> {Registered, Open, Closed, Archived}

  sealed interface Command extends Domain.Command permits Commands.Change, Commands.Close, Commands.Open, Commands.Register, Commands.Reopen {}
  sealed interface Event extends Domain.Event permits Events.Archived, Events.Closed, Events.Changed, Events.Opened, Events.Registered, Events.Reopened {}

  record ID(UUID value) implements Domain.ID<UUID> {}
  record Name(String value) implements Attribute<String> {}
  record Description(String value) implements Attribute<String> {}
  record Details(Name name, Description description) {}

  F register(Forum.Name name, Forum.Description description);
  F change(Forum.Name name, Forum.Description description);
  F open();
  F close();
  F archive();
  F reopen();

  record Model(ID id, Details details) {
    public Model(ID id) {this(id, null);}
  }
}

