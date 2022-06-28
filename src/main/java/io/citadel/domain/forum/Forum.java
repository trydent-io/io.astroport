package io.citadel.domain.forum;

import io.citadel.domain.forum.Forum.Event.Archived;
import io.citadel.domain.forum.Forum.Event.Closed;
import io.citadel.domain.forum.Forum.Event.Opened;
import io.citadel.domain.forum.Forum.Event.Registered;
import io.citadel.domain.forum.Forum.Event.Reopened;
import io.citadel.domain.forum.Forum.Event.Replaced;
import io.citadel.domain.member.Member;
import io.citadel.kernel.eventstore.EventStorePool;
import io.citadel.kernel.eventstore.Metadata;
import io.citadel.kernel.eventstore.metadata.Aggregate;
import io.citadel.kernel.eventstore.metadata.Version;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.time.LocalDateTime;
import java.util.UUID;

import static io.citadel.kernel.eventstore.metadata.Aggregate.*;

public sealed interface Forum {
  public static String NAME = "forum";

  static ID id(final String value) {
    return new ID(UUID.fromString(value));
  }

  static Entity entity(JsonObject json) {
    return json.mapTo(Entity.class);
  }

  static State state(String value) {
    return Forum.State.valueOf(value);
  }

  record Entity(Details details, Member.ID registerer) {}

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
    record Register(Name name, Description description, LocalDateTime at) implements Command {
    }

    record Open(LocalDateTime at) implements Command {
    }

    record Replace(Name name, Description description) implements Command {
    }

    record Close(LocalDateTime at) implements Command {
    }

    record Reopen(LocalDateTime at, Member.ID memberID) implements Command {
    }

    record Archive() implements Command {
    }
  }

  sealed interface Event {
    enum Names {Opened, Closed, Registered, Reopened, Replaced, Archived}

    record Registered(Details details) implements Event {
    }

    record Replaced(Details details) implements Event {
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
}

final class Lookup implements Forum {
  private final EventStorePool pool;

  Lookup(EventStorePool pool) {
    this.pool = pool;
  }

  @Override
  public Future<Forum> load(Forum.ID id) {
    return pool.query(id, Forum.NAME).map(aggregate ->
      switch (aggregate) {
        case Zero zero -> Aggregate.root(zero.id().as(Forum::id), null, State.Registered, Version.Zero);
        case Last last -> Aggregate.root(last.id().as(Forum::id), last.entity().as(Forum::entity), last.state().as(Forum::state), last.version());
      }
    );
  }
}
