package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.kernel.vertx.Task;
import io.vertx.core.Future;

import static io.citadel.domain.forum.Forum.State.Archived;
import static io.citadel.domain.forum.Forum.State.Closed;
import static io.citadel.domain.forum.Forum.State.Open;
import static io.citadel.domain.forum.Forum.State.Registered;

public record Staging(State state) implements Forum.Lifecycle, Task {
  public Staging() {this(null);}

  @Override
  public Future<Lifecycle> register(Details details) {
    return switch (state) {
      case null -> success(new Staging(Registered));
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
      case Registered -> success(new Staging(Open));
      default -> failure("Can't apply open, lifecycle is not registered");
    };
  }

  @Override
  public Future<Lifecycle> close() {
    return switch (state) {
      case Open -> success(new Staging(Closed));
      default -> failure("Can't apply close, lifecycle is not open");
    };
  }

  @Override
  public Future<Lifecycle> archive() {
    return switch (state) {
      case Closed -> success(new Staging(Archived));
      default -> failure("Can't apply archive, lifecycle is not closed");
    };
  }

  @Override
  public Future<Lifecycle> reopen() {
    return switch (state) {
      case Closed -> success(new Staging(Open));
      default -> failure("Can't apply reopen, lifecycle is not closed");
    };
  }
}
