package io.citadel.domain.forum;

import io.citadel.domain.forum.handler.Commands;
import io.citadel.domain.forum.handler.Events;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.domain.attribute.Attribute;
import io.citadel.kernel.eventstore.Context;
import io.citadel.kernel.eventstore.Lookup;
import io.citadel.kernel.func.ThrowablePredicate;
import io.vertx.core.Future;

import java.util.Optional;
import java.util.UUID;

public interface Forum extends Domain.Aggregate<Forum.ID, Forum.Model, Forum.Event, Forum> {
  Commands commands = Commands.Companion;
  Events events = Events.Companion;
  Defaults defaults = Defaults.Companion;

  static Forum create(Lookup lookup) {
    return new Root(lookup);
  }

  enum State implements Domain.State<Forum.State, Forum.Event> {
    Registered, Open, Closed, Archived;

    @Override
    public Optional<Forum.State> next(final Event event) {
      return Optional.ofNullable(
        switch (event) {
          case Events.Registered it && this.is(Registered) -> this;
          case Events.Replaced it && this.is(Registered, Open) -> this;
          case Events.Opened it && this.is(Registered) -> Open;
          case Events.Closed it && this.is(Open) -> Closed;
          case Events.Reopened it && this.is(Closed) -> Registered;
          case Events.Archived it && this.is(Closed) -> Archived;
          default -> null;
        }
      );
    }
  }

  sealed interface Command extends Domain.Command permits Commands.Replace, Commands.Archive, Commands.Close, Commands.Open, Commands.Register, Commands.Reopen {
  }

  sealed interface Event extends Domain.Event permits Events.Archived, Events.Closed, Events.Replaced, Events.Opened, Events.Registered, Events.Reopened {
  }

  record ID(UUID value) implements Domain.ID<UUID> {
  } // ID

  record Name(String value) implements Attribute<String> {
  } // part of Details

  record Description(String value) implements Attribute<String> {
  } // part of Details

  record Details(Name name, Description description) {
  } // ValueObject for Details

  record Model(Forum.ID id, Forum.Details details) implements Domain.Model<Forum.ID> {
    public Model(Forum.ID id) {
      this(id, null);
    }
  }
}

final class Root implements Forum {
  public static final String FORUM = "forum";
  private final Lookup lookup;

  Root(Lookup lookup) {
    this.lookup = lookup;
  }

  @Override
  public Future<Context<Forum.Model, Forum.Event>> load(ID id, long version) {
    return lookup.findSnapshot(id, FORUM, version)
      .map(it -> it.<Forum.Model, Forum.Event>normalize(Forum.events::convert))
      .map(it -> it.identity(Model::new))
      .map(it -> it.hydrate(State.Registered, Forum.defaults::snapshot));
  }

  public static void main(String[] args) {
    new Root(null)
      .load(new Forum.ID(UUID.randomUUID()))
      .map(context -> context.has(descriptionLengthGreaterThanZero()))
  }

  private static ThrowablePredicate<Model> descriptionLengthGreaterThanZero() {
    return model -> model.details().description().value().length() > 0;
  }
}

