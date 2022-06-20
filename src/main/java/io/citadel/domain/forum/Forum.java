package io.citadel.domain.forum;

import io.citadel.domain.member.Member;
import io.citadel.kernel.domain.State;
import io.vertx.core.json.JsonObject;

import java.time.LocalDateTime;
import java.util.UUID;

public sealed interface Forum {
  ID id();
  Details details();
  Member.ID registeredBy();

  String FORUM = "forum";
  static ID id(final String value) { return new ID(UUID.fromString(value)); }
  static Forum model(ID id) { return new Model(id, null, null); }
  static Event event(String name, JsonObject json) {
    return switch (Event.Names.valueOf(name)) {
      case Opened -> json.mapTo(Event.Opened.class);
      case Closed -> json.mapTo(Event.Closed.class);
      case Registered -> json.mapTo(Event.Registered.class);
      case Reopened -> json.mapTo(Event.Reopened.class);
      case Replaced -> json.mapTo(Event.Replaced.class);
      case Archived -> json.mapTo(Event.Archived.class);
    };
  }
  static Forum attach(Forum forum, Forum.Event event) {
    return switch (event) {
      case Event.Registered it ->  new Model(forum.id(), it.details(), null);
      case Event.Replaced it -> new Model(forum.id(), it.details(), null);
      case Event.Opened it -> forum;
      case Event.Closed it -> forum;
      case Event.Reopened it -> forum;
      case Event.Archived it -> forum;
    };
  }

  static Invariants entry() {
    return Invariants.Registered;
  }

  enum Invariants implements State<Invariants, Event> {
    Registered, Open, Closed, Archived;

    @Override
    public Invariants transit(final Event event) {
      return switch (event) {
          case Event.Registered it && this.is(Registered) -> this;
          case Event.Replaced it && this.is(Registered, Open) -> this;
          case Event.Opened it && this.is(Registered) -> Open;
          case Event.Closed it && this.is(Open) -> Closed;
          case Event.Reopened it && this.is(Closed) -> Registered;
          case Event.Archived it && this.is(Closed) -> Archived;
          default -> throw new IllegalStateException("Can't transit to next state, state %s and event %s don't satisfy invariant".formatted(this, event));
        };
    }
  }

  sealed interface Command {
    record Register(Forum.Name name, Forum.Description description, LocalDateTime at) implements Forum.Command {}
    record Open(LocalDateTime at) implements Forum.Command {}
    record Replace(Forum.Name name, Forum.Description description) implements Forum.Command {}
    record Close(LocalDateTime at) implements Forum.Command {}
    record Reopen(LocalDateTime at, Member.ID memberID) implements Forum.Command {}
    record Archive() implements Forum.Command {}
  }

  sealed interface Event {
    enum Names {Opened, Closed, Registered, Reopened, Replaced, Archived}
    record Registered(Forum.Details details) implements Forum.Event {}
    record Replaced(Forum.Details details) implements Forum.Event {}
    record Opened() implements Forum.Event {}
    record Closed() implements Forum.Event {}
    record Reopened() implements Forum.Event {}
    record Archived() implements Forum.Event {}
  }

  record ID(UUID value) {
  } // ID
  record Name(String value) {
  } // part of Details
  record Description(String value) {
  } // part of Details
  record Details(Name name, Description description) {
  } // ValueObject for Details
}

record Model(Forum.ID id, Forum.Details details, Member.ID registeredBy) implements Forum {}
