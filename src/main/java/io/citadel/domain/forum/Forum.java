package io.citadel.domain.forum;

import io.citadel.domain.forum.handler.Commands;
import io.citadel.domain.forum.handler.Events;
import io.citadel.domain.member.Member;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.eventstore.Context;
import io.citadel.kernel.eventstore.Lookup;
import io.citadel.kernel.func.ThrowableFunction;
import io.vertx.core.Future;

import java.util.Optional;
import java.util.UUID;

public interface Forum extends Domain.Aggregate<Forum.ID, Forum.Model, Forum.Event, Forum> {
  Commands commands = Commands.Companion;
  Events event = Events.Companion;
  Defaults defaults = Defaults.Companion;

  static Forum create(Lookup lookup) {
    return new Root(lookup);
  }

  enum State implements io.citadel.kernel.domain.State<State, Event> {
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

  sealed interface Command permits Commands.Replace, Commands.Archive, Commands.Close, Commands.Open, Commands.Register, Commands.Reopen {
  }

  sealed interface Event permits Events.Archived, Events.Closed, Events.Replaced, Events.Opened, Events.Registered, Events.Reopened {
  }

  record ID(UUID value) {
  } // ID
  record Name(String value) {
  } // part of Details
  record Description(String value) {
  } // part of Details
  record Details(Name name, Description description) {
  } // ValueObject for Details

  record Model(Forum.ID id, Forum.Details details, Member.ID registerer) {
    public Model(Forum.ID id) {
      this(id, null, null);
    }
  }

  <T> Future<T> registeredBy(ThrowableFunction<? super Member, ? extends T> function);
}

final class Root implements Forum {
  public static final String FORUM = "forum";
  private final Lookup lookup;
  private final Context<Model, Event> context;
  private final Member member;

  Root(Lookup lookup, Context<Model, Event> context, Member member) {
    this.lookup = lookup;
    this.context = context;
    this.member = member;
  }

  @Override
  public Future<Forum> load(ID id, long version) {
    return context(id, version).map(context -> new Root(lookup, context, member));
  }

  @Override
  public Future<Forum> reload() {
    return context.load(model -> load(model.id()));
  }

  private Future<Context<Model, Event>> context(UUID forumId, long version) {
    return lookup.find(FORUM, forumId, version)
      .map(it -> it.<Model, Event>deserializes(Forum.event::fromJson))
      .map(it -> it.creates(id -> id.))
      .map(it -> it.hydrate(State.Registered, Forum.defaults::snapshot));
  }

  @Override
  public <T> Future<T> registeredBy(ThrowableFunction<? super Member, ? extends T> then) {
    return context.load(model -> member.load(model.registerer()).map(then::apply));
  }
}

