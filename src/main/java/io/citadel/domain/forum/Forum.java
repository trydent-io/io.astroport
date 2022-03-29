package io.citadel.domain.forum;

import io.citadel.domain.forum.aggregate.Defaults;
import io.citadel.domain.forum.aggregate.Providing;
import io.citadel.domain.forum.aggregate.Sourcing;
import io.citadel.domain.forum.aggregate.Staging;
import io.citadel.domain.forum.command.Commands;
import io.citadel.domain.forum.event.Events;
import io.citadel.domain.forum.model.Attributes;
import io.citadel.domain.forum.repository.History;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.domain.attribute.Attribute;

import java.util.Optional;
import java.util.UUID;

public sealed interface Forum permits Forum.Transaction, Forum.Snapshot {
  String AGGREGATE_NAME = "FORUM";

  Commands commands = Commands.Defaults;
  Events event = Events.Defaults;
  Attributes attributes = Attributes.Defaults;
  Defaults defaults = Defaults.Defaults;

  static Forum with(Forum.ID id) {
    return Forum.defaults.snapshot(id);
  }

  enum State implements Domain.State<State> {Registered, Open, Closed, Archived}

  sealed interface Command extends Domain.Command permits Commands.Close, Commands.Edit, Commands.Open, Commands.Register, Commands.Reopen {}

  sealed interface Event extends Domain.Event permits Events.Archived, Events.Closed, Events.Edited, Events.Opened, Events.Registered, Events.Reopened {}

  sealed interface Hydration extends Domain.Hydration<Forum.Snapshot> permits History {}

  record ID(UUID value) implements Domain.ID<Forum.ID> {}

  record Name(String value) implements Attribute<String> {}

  record Description(String value) implements Attribute<String> {}

  record Details(Name name, Description description) implements Domain.ValueObject<Details> {}

  record Model(Forum.ID id, Forum.Details details) {}

  sealed interface Snapshot extends Forum, Domain.Snapshot<Transaction> permits Sourcing, Staging {}

  sealed interface Transaction extends Forum, Domain.Transaction permits Providing, Staging {}

  Forum register(Forum.Name name, Forum.Description description);
  Forum edit(Forum.Name name, Forum.Description description);
  Forum open();
  Forum close();
  Forum archive();
  Forum reopen();

  default Forum.Snapshot asSnapshot() {
    return this instanceof Snapshot snapshot
      ? snapshot
      : null;
  }

  default Forum.Transaction asTransaction() {
    return this instanceof Transaction transaction ? transaction : null;
  }

  public static void main(String[] args) {
    final var forums = Forums.repository(null, null);
    Forum.with(new Forum.ID(UUID.randomUUID()))
      .register(new Name("ciao"), new Description("è un saluto"))
      .asSnapshot()
      .open()
      .asTransaction()
      .commit(forums::save);
  }
}

