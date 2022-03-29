package io.citadel.domain.forum;

import io.citadel.domain.forum.command.Commands;
import io.citadel.domain.forum.event.Events;
import io.citadel.domain.forum.model.Attributes;
import io.citadel.domain.forum.repository.Sourcing;
import io.citadel.domain.forum.state.States;
import io.citadel.eventstore.data.MetaAggregate;
import io.citadel.eventstore.data.MetaEvent;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.domain.attribute.Attribute;
import io.citadel.kernel.func.ThrowableBiFunction;
import io.vertx.core.Future;

import java.util.UUID;
import java.util.stream.Stream;

import static io.citadel.domain.forum.Forum.State.Closed;
import static io.citadel.domain.forum.Forum.State.Open;
import static io.citadel.domain.forum.Forum.State.Registered;
import static java.util.Objects.isNull;

public sealed interface Forum extends Domain.Aggregate<Forum> permits Forum.Service, Forum.Snapshot, History, Staging {
  String AGGREGATE_NAME = "FORUM";

  Commands commands = Commands.Defaults;
  Events event = Events.Defaults;
  Attributes attributes = Attributes.Defaults;
  States states = States.Defaults;

  static Forum with(Forum.ID id) { return null; }

  enum State implements Domain.State<State> {Registered, Open, Closed, Archived}

  sealed interface Command extends Domain.Command permits Commands.Close, Commands.Edit, Commands.Open, Commands.Register, Commands.Reopen {}
  sealed interface Event extends Domain.Event permits Events.Archived, Events.Closed, Events.Edited, Events.Opened, Events.Registered, Events.Reopened {}
  sealed interface Hydration extends Domain.Hydration<Forum> permits Sourcing {}

  record ID(UUID value) implements Domain.ID<Forum.ID> {}
  record Name(String value) implements Attribute<String> {}
  record Description(String value) implements Attribute<String> {}
  record Details(Name name, Description description) implements Domain.ValueObject<Details> {}

  record Model(Forum.ID id, Forum.Details details) {}

  sealed interface Snapshot extends Forum, Domain.Snapshot<Forum> {}
  sealed interface Service extends Forum, Domain.Transaction<Forum> {}

  Forum register(Forum.Details details);
  Forum open();
  Forum close();
  Forum archive();
  Forum reopen();
}

record History(Forum.Model model) implements Forum.Snapshot {
  @Override
  public Forum register(final Details details) {
    return new History(new Model(model.id(), details));
  }

  @Override
  public Forum open() {
    return this;
  }

  @Override
  public Forum close() {
    return this;
  }

  @Override
  public Forum archive() {
    return this;
  }

  @Override
  public Forum reopen() {
    return this;
  }

  @Override
  public Forum freeze(final long version) {
    return new ;
  }
}

record Staging(Forum.State state, Forum forum) implements Forum.Snapshot, Forum.Service {
  @Override
  public Forum register(final Details details) {
    return isNull(state) ? new Staging(Registered, forum.register(details)) : null;
  }

  @Override
  public Forum open() {
    return Registered.is(state) ? new Staging(State.Open, forum.open()) : null;
  }

  @Override
  public Forum close() {
    return Open.is(state) ? new Staging(State.Closed, forum.close()) : null;
  }

  @Override
  public Forum archive() {
    return Closed.is(state) ? new Staging(State.Archived, forum.archive()) : null;
  }

  @Override
  public Forum reopen() {
    return Closed.is(state) ? new Staging(Open, forum.reopen()) : null;
  }

  @Override
  public Forum.Service freeze(final long version) {
    return new Staging(state, switch (forum) {
      case Forum.Snapshot snapshot -> snapshot.freeze(version);
      default -> null;
    });
  }

  @Override
  public Future<Void> commit(final ThrowableBiFunction<? super MetaAggregate, ? super Stream<MetaEvent>, ? extends Future<Void>> commit) {
    return null;
  }
}

record Committable(Forum.Model model, long version, Stream<Forum.Event> events) implements Forum.Service {
  @Override
  public Forum register(final Details details) {
    return null;
  }

  @Override
  public Forum open() {
    return null;
  }

  @Override
  public Forum close() {
    return null;
  }

  @Override
  public Forum archive() {
    return null;
  }

  @Override
  public Forum reopen() {
    return null;
  }

  @Override
  public Future<Void> commit(final ThrowableBiFunction<? super MetaAggregate, ? super Stream<MetaEvent>, ? extends Future<Void>> commit) {
    return commit.apply(null, null);
  }
}
