package io.citadel.domain.forum;

import io.citadel.domain.forum.Forum.Event.*;
import io.citadel.domain.forum.handler.command.Register;
import io.citadel.domain.member.Member;
import io.citadel.kernel.domain.Committable;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.domain.Transaction;
import io.citadel.kernel.vertx.Task;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.time.LocalDateTime;
import java.util.UUID;

public sealed interface Forum {
  enum Namespace implements Forum {}
  String NAME = "forum";

  record Entity(ID id, Details details) {
    Entity(ID id) { this(id, null); }
    public Entity details(Name name, Description description) {
      return new Entity(id, new Details(name, description));
    }
  }

  enum State implements io.citadel.kernel.domain.State<State, Event> {
    Registered, Open, Closed, Archived;

    @Override
    public State transit(final Event event) {
      return switch (event) {
        case Registered it && this.is(Registered) -> this;
        case Replaced it && this.is(Registered, Open) -> this;
        case Opened it && this.is(Registered) -> Open;
        case Closed it && this.is(Open) -> Closed;
        case Reopened it && this.is(Closed) -> Registered;
        case Archived it && this.is(Closed) -> Archived;
        default -> throw new IllegalStateException("Can't transit to next state, state %s and event %s don't satisfy invariant".formatted(this, event));
      };
    }
  }

  sealed interface Command {
    record Register(Name name, Description description, LocalDateTime at) implements Command {}
    record Open(LocalDateTime at) implements Command {}
    record Replace(Name name, Description description) implements Command {}
    record Close(LocalDateTime at) implements Command {}
    record Reopen(LocalDateTime at, Member.ID memberID) implements Command {}
    record Archive() implements Command {}
  }

  sealed interface Event {
    enum Names {Opened, Closed, Registered, Reopened, Replaced, Archived}

    record Registered(Name name, Description description, Member.ID by) implements Event {
    }

    record Replaced(Name name, Description description, Member.ID by) implements Event {
    }

    record Opened() implements Event {
    }

    record Closed() implements Event {
    }

    record Reopened() implements Event {
    }

    record Archived() implements Event {
    }
  }

  record ID(UUID value) {
  } // ID

  record Name(String value) {
  } // part of Details

  record Description(String value) {
  } // part of Details

  record Details(Name name, Description description) {
  } // ValueObject for Details

  sealed interface Handler<C extends Record & Command> extends Domain.Handler<Forum, C> permits Register {
  }
}


