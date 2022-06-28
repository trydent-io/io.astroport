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
import io.citadel.kernel.eventstore.metadata.Change;
import io.citadel.kernel.eventstore.metadata.Version;
import io.citadel.kernel.vertx.Task;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static io.citadel.kernel.eventstore.metadata.Aggregate.*;

public sealed interface Forum {
  String FORUM = "forum";

  static ID id(final String value) {
    return new ID(UUID.fromString(value));
  }

  static Model model(JsonObject json) {
    return json.mapTo(Model.class);
  }

  static State state(String value) {
    return Forum.State.valueOf(value);
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

  record Model(Details details, Member.ID registeredBy) {
  }

  default Future<Forum> load(Forum.ID id) {
    return Future.succeededFuture(this);
  }
  default Future<Forum> has(Predicate<? super Model> predicate) {
    return Future.succeededFuture(this);
  }
  default Future<Forum> log(Function<? super Model, ? extends Forum.Event> function) {
    return Future.succeededFuture(this);
  }

  static Forum aggregate(Forum.ID id, Version version) {
    return new Aggregate(id, null, State.Registered, version, Stream.empty());
  }
  static Forum aggregate(Forum.ID id, Forum.Model model, Forum.State state, Version version) {
    return new Aggregate(id, model, state, version, Stream.empty());
  }
}

final class Lookup implements Forum {
  private final EventStorePool client;

  Lookup(EventStorePool client) {
    this.client = client;
  }

  @Override
  public Future<Forum> load(Forum.ID id) {
    return client.query(id, Forum.FORUM, Version.Last.value()).map(aggregate ->
      switch (aggregate) {
        case Empty it -> Forum.aggregate(it.id().as(Forum::id), it.version());
        case Entity it -> Forum.aggregate(
          it.id().as(Forum::id),
          it.model().as(Forum::model),
          it.state().as(Forum::state),
          it.version()
        );
      }
    );
  }
}

final class Aggregate implements Forum, Task {
  private final Forum.ID id;
  private final Forum.Model model;
  private final Forum.State state;
  private final Version version;
  private final Stream<Change> changes;
  Aggregate(ID id, Model model, State state, Version version, Stream<Change> changes) {
    this.id = id;
    this.model = model;
    this.state = state;
    this.version = version;
    this.changes = changes;
  }
  @Override
  public Future<Forum> load(ID id) { return success(this); }

  @Override
  public Future<Forum> has(Predicate<? super Model> predicate) {
    return predicate.test(model) ? success(this) : failure("Can't validate condition");
  }

  @Override
  public Future<Forum> log(Function<? super Model, ? extends Event> function) {
    return model != null
      ? new Aggregate(id, model, state, version, Stream.concat(changes, Stream.of(Change.of(id.value().toString(),  )))) function.apply(model);
  }
}


