package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.eventstore.data.MetaAggregate;
import io.citadel.eventstore.data.MetaEvent;
import io.citadel.kernel.func.ThrowableBiFunction;
import io.vertx.core.Future;

import java.util.stream.Stream;

import static io.citadel.domain.forum.Forum.State.Open;
import static io.citadel.domain.forum.Forum.State.Registered;
import static io.vertx.core.Future.failedFuture;

public record Staging(State state, Forum forum) implements Forum.Snapshot, Forum.Transaction {
  @Override
  public Forum register(final Name name, Description description) {
    return switch (state) {
      case null -> new Staging(Registered, forum.register(name, description));
      default -> null;
    };
  }

  @Override
  public Forum edit(final Name name, Description description) {
    return switch (state) {
      case Registered, Open -> new Staging(state, forum.edit(name, description));
      default -> null;
    };
  }

  @Override
  public Forum open() {
    return switch (state) {
      case Registered -> new Staging(State.Open, forum.open());
      default -> null;
    };
  }

  @Override
  public Forum close() {
    return switch (state) {
      case Open -> new Staging(State.Closed, forum.close());
      default -> null;
    };
  }

  @Override
  public Forum archive() {
    return switch (state) {
      case Closed -> new Staging(State.Archived, forum.archive());
      default -> null;
    };
  }

  @Override
  public Forum reopen() {
    return switch (state) {
      case Closed -> new Staging(Open, forum.reopen());
      default -> null;
    };
  }

  @Override
  public Transaction transaction(final long version) {
    return new Staging(state, switch (forum) {
      case Snapshot snapshot -> snapshot.transaction(version);
      default -> null;
    });
  }

  @Override
  public Future<Void> commit(final ThrowableBiFunction<? super MetaAggregate, ? super Stream<MetaEvent>, ? extends Future<Void>> transaction) {
    return switch (forum) {
      case Transaction service -> service.commit(transaction);
      default -> failedFuture("Can't commit");
    };
  }
}
