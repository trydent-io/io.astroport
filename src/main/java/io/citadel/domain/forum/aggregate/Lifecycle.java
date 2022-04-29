package io.citadel.domain.forum.aggregate;

import io.citadel.CitadelException;
import io.citadel.domain.forum.Forum;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.func.Maybe;
import io.citadel.kernel.func.ThrowableFunction;
import io.citadel.kernel.vertx.Task;
import io.vertx.core.Future;

import static io.citadel.domain.forum.Forum.State.Archived;
import static io.citadel.domain.forum.Forum.State.Closed;
import static io.citadel.domain.forum.Forum.State.Open;
import static io.citadel.domain.forum.Forum.State.Registered;

public record Lifecycle(State state) implements Forum<Lifecycle>, Task {
  public Lifecycle() {this(null);}

  @Override
  public Future<Lifecycle> register(Details details) {
    return switch (state) {
      case null -> success(new Lifecycle(Registered));
      default -> failure("Can't apply register, lifecycle is identity");
    };
  }

  @Override
  public Future<Lifecycle> replace(Details details) {
    return switch (state) {
      case Registered, Open -> success(this);
      default -> failure("Can't apply replace, lifecycle is not in its boundaries");
    };
  }

  @Override
  public Future<Lifecycle> open() {
    return switch (state) {
      case Registered -> success(new Lifecycle(Open));
      default -> failure("Can't apply open, lifecycle is not registered");
    };
  }

  @Override
  public Future<Lifecycle> close() {
    return switch (state) {
      case Open -> success(new Lifecycle(Closed));
      default -> failure("Can't apply close, lifecycle is not open");
    };
  }

  @Override
  public Future<Lifecycle> archive() {
    return switch (state) {
      case Closed -> success(new Lifecycle(Archived));
      default -> failure("Can't apply archive, lifecycle is not closed");
    };
  }

  @Override
  public Future<Lifecycle> reopen() {
    return switch (state) {
      case Closed -> success(new Lifecycle(Open));
      default -> failure("Can't apply reopen, lifecycle is not closed");
    };
  }
}
