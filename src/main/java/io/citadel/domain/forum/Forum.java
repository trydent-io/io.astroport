package io.citadel.domain.forum;

import io.citadel.domain.forum.aggregate.Defaults;
import io.citadel.domain.forum.aggregate.Snapshot;
import io.citadel.domain.forum.aggregate.Stage;
import io.citadel.domain.forum.handler.Commands;
import io.citadel.domain.forum.handler.Events;
import io.citadel.domain.forum.model.Attributes;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.domain.attribute.Attribute;

import java.util.UUID;

public sealed interface Forum extends Domain.Timeline<Forum.Event> permits Snapshot, Stage {
  Commands commands = Commands.Companion;
  Events events = Events.Companion;
  Attributes attributes = Attributes.Companion;
  Defaults defaults = Defaults.Companion;

  enum State implements Domain.State<Forum.State> {Registered, Open, Closed, Archived}

  sealed interface Command extends Domain.Command permits Commands.Replace, Commands.Archive, Commands.Close, Commands.Open, Commands.Register, Commands.Reopen {}
  sealed interface Event extends Domain.Event permits Events.Archived, Events.Closed, Events.Replaced, Events.Opened, Events.Registered, Events.Reopened {}

  interface Aggregate extends Domain.Aggregate<Forum.Model, Forum.Event> {}

  record ID(UUID value) implements Domain.ID<UUID> {} // ID
  record Name(String value) implements Attribute<String> {} // part of Details
  record Description(String value) implements Attribute<String> {} // part of Details
  record Details(Name name, Description description) {} // ValueObject for Details

  record Model(Forum.ID id, Forum.Details details) implements Domain.Model<Forum.ID>, Domain.Timeline<Event, Model> {
    public Model(Forum.ID id) {this(id, null);}

    @SuppressWarnings("DuplicateBranchesInSwitch")
    @Override
    public Domain.Timeline<Event, Model> take(Event event) {
      return switch (event) {
        case Events.Archived archived -> this;
        case Events.Registered registered -> new Model(id, registered.details());
        case Events.Reopened reopened -> this;
        case Events.Opened opened -> this;
        case Events.Closed closed -> this;
        case Events.Replaced replaced -> new Model(id, replaced.details());
      };
    }

    @Override
    public Model get() {
      return this;
    }
  }
}

