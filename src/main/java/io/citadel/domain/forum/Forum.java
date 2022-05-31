package io.citadel.domain.forum;

import io.citadel.domain.forum.handler.Commands;
import io.citadel.domain.forum.handler.Events;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.domain.actor.Actor;
import io.citadel.kernel.domain.attribute.Attribute;
import io.citadel.kernel.func.ThrowableBiFunction;
import io.citadel.kernel.func.ThrowableFunction;
import io.vertx.core.json.JsonObject;

import java.util.Optional;
import java.util.UUID;

public interface Forum extends Actor<Forum.ID, Forum.Model, Forum.Event, Forum.State> {
  Commands commands = Commands.Companion;
  Events events = Events.Companion;
  Defaults defaults = Defaults.Companion;

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

  sealed interface Command extends Domain.Command permits Commands.Replace, Commands.Archive, Commands.Close, Commands.Open, Commands.Register, Commands.Reopen {}
  sealed interface Event extends Domain.Event permits Events.Archived, Events.Closed, Events.Replaced, Events.Opened, Events.Registered, Events.Reopened {}

  interface Transaction extends Domain.Transaction<Model, Event> {}

  record ID(UUID value) implements Domain.ID<UUID> {} // ID
  record Name(String value) implements Attribute<String> {} // part of Details
  record Description(String value) implements Attribute<String> {} // part of Details
  record Details(Name name, Description description) {} // ValueObject for Details

  record Model(Forum.ID id, Forum.Details details) implements Domain.Model<Forum.ID> {
    public Model(Forum.ID id) {this(id, null);}
  }

  @Override
  default ThrowableFunction<? super ID, ? extends Model> identity() {
    return Model::new;
  }

  @Override
  default ThrowableBiFunction<? super String, ? super JsonObject, ? extends Event> normalize() {
    return Forum.events::from;
  }

  @Override
  default ThrowableBiFunction<? super Model, ? super Event, ? extends Model> hydrate() {
    return Forum.defaults::snapshot;
  }

  @Override
  default State initial() {
    return State.Registered;
  }
}

