package io.citadel.domain.forum;

import io.citadel.domain.forum.aggregate.Aggregate;
import io.citadel.domain.forum.aggregate.Defaults;
import io.citadel.domain.forum.aggregate.Lifecycle;
import io.citadel.domain.forum.aggregate.Snapshot;
import io.citadel.domain.forum.aggregate.Span;
import io.citadel.domain.forum.aggregate.Transaction;
import io.citadel.domain.forum.handler.Commands;
import io.citadel.domain.forum.handler.Events;
import io.citadel.domain.forum.model.Attributes;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.domain.attribute.Attribute;
import io.citadel.kernel.func.Maybe;

import java.util.UUID;

public sealed interface Forum<F extends Forum<F>> permits Span, Aggregate, Lifecycle, Snapshot, Transaction {
  String AGGREGATE_NAME = "FORUM";

  Commands commands = Commands.Companion;
  Events events = Events.Companion;
  Attributes attributes = Attributes.Companion;
  Defaults defaults = Defaults.Companion;

  enum State implements Domain.State<Forum.State> {Registered, Open, Closed, Archived}

  sealed interface Command extends Domain.Command permits Commands.Replace, Commands.Archive, Commands.Close, Commands.Open, Commands.Register, Commands.Reopen {}
  sealed interface Event extends Domain.Event permits Events.Archived, Events.Closed, Events.Replaced, Events.Opened, Events.Registered, Events.Reopened {}

  record ID(UUID value) implements Domain.ID<UUID> {} // ID
  record Name(String value) implements Attribute<String> {} // part of Details
  record Description(String value) implements Attribute<String> {} // part of Details
  record Details(Name name, Description description) {} // ValueObject for Details

  Maybe<F> register(Forum.Details details);
  Maybe<F> replace(Forum.Details details);
  Maybe<F> open();
  Maybe<F> close();
  Maybe<F> archive();
  Maybe<F> reopen();

  record Model(ID id, Details details) {
    public Model(ID id) {this(id, null);}
  }
}

