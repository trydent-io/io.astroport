package io.citadel.domain.forum;

import io.citadel.domain.forum.aggregate.Aggregate;
import io.citadel.domain.forum.aggregate.Defaults;
import io.citadel.domain.forum.aggregate.Life;
import io.citadel.domain.forum.aggregate.Snap;
import io.citadel.domain.forum.command.Commands;
import io.citadel.domain.forum.event.Events;
import io.citadel.domain.forum.model.Attributes;
import io.citadel.domain.forum.repository.History;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.domain.attribute.Attribute;

import java.util.UUID;

public sealed interface Forum<F extends Forum<?>> extends Domain.Model permits Aggregate, Life, Snap {
  String AGGREGATE_NAME = "FORUM";

  Commands commands = Commands.Companion;
  Events event = Events.Companion;
  Attributes attributes = Attributes.Companion;
  Defaults defaults = Defaults.Companion;

  static Snap with(Forum.ID id) {
    return Forum.defaults.snapshot(id);
  }

  enum State implements Domain.State<State> {Registered, Open, Closed, Archived}

  sealed interface Command extends Domain.Command permits Commands.Close, Commands.Edit, Commands.Open, Commands.Register, Commands.Reopen {}
  sealed interface Event extends Domain.Event permits Events.Archived, Events.Closed, Events.Edited, Events.Opened, Events.Registered, Events.Reopened {}
  sealed interface Hydration extends Domain.Hydration<Aggregate> permits History {}

  record ID(UUID value) implements Domain.ID<UUID> {
    public static ID random() { return new ID(UUID.randomUUID()); }
  }
  record Name(String value) implements Attribute<String> {}
  record Description(String value) implements Attribute<String> {}
  record Details(Name name, Description description) {}
  record Model(Forum.ID id, Forum.Details details) {}

  F register(Forum.Name name, Forum.Description description);
  F edit(Forum.Name name, Forum.Description description);
  F open();
  F close();
  F archive();
  F reopen();

  static void main(String[] args) {
    final var forums = Forums.repository(null, null);
    Forum.with(Forum.ID.random())
      .register(new Name("ciao"), new Description("è un saluto"))
      .aggregate(12)
      .open()
      .commit(forums::save);
  }
}

