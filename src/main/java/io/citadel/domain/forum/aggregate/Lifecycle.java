package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.eventstore.data.AggregateInfo;
import io.citadel.eventstore.data.EventInfo;
import io.citadel.kernel.func.ThrowableBiFunction;
import io.vertx.core.Future;

import java.util.stream.Stream;

import static io.citadel.domain.forum.Forum.State.Open;
import static io.citadel.domain.forum.Forum.State.Registered;
import static io.vertx.core.Future.failedFuture;

public record Lifecycle(State state, Forum forum) implements Forum.Aggregate, Forum.Service {
  @Override
  public Forum register(final Name name, Description description) {
    return switch (state) {
      case null -> new Lifecycle(Registered, forum.register(name, description));
      default -> null;
    };
  }

  @Override
  public Forum edit(final Name name, Description description) {
    return switch (state) {
      case Registered, Open -> new Lifecycle(state, forum.edit(name, description));
      default -> null;
    };
  }

  @Override
  public Forum open() {
    return switch (state) {
      case Registered -> new Lifecycle(State.Open, forum.open());
      default -> null;
    };
  }

  @Override
  public Forum close() {
    return switch (state) {
      case Open -> new Lifecycle(State.Closed, forum.close());
      default -> null;
    };
  }

  @Override
  public Forum archive() {
    return switch (state) {
      case Closed -> new Lifecycle(State.Archived, forum.archive());
      default -> null;
    };
  }

  @Override
  public Forum reopen() {
    return switch (state) {
      case Closed -> new Lifecycle(Open, forum.reopen());
      default -> null;
    };
  }

  @Override
  public Service service(final long version) {
    return new Lifecycle(state, switch (forum) {
      case Aggregate snapshot -> snapshot.service(version);
      default -> throw new IllegalStateException("Can't provide service for aggregate Forum");
    });
  }

  @Override
  public Future<Void> commit(final ThrowableBiFunction<? super AggregateInfo, ? super Stream<EventInfo>, ? extends Future<Void>> transaction) {
    return switch (forum) {
      case Service service -> service.commit(transaction);
      default -> failedFuture("Can't commit");
    };
  }
}
