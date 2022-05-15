package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;

import static io.citadel.domain.forum.Forum.State.Archived;
import static io.citadel.domain.forum.Forum.State.Closed;
import static io.citadel.domain.forum.Forum.State.Open;
import static io.citadel.domain.forum.Forum.State.Registered;

public record Staging(State state) implements Forum.Lifecycle {
  public Staging() {this(null);}

  @Override
  public Lifecycle register(Details details) {
    return switch (state) {
      case null -> new Staging(Registered);
      default -> throw new IllegalStateException("Can't apply register, lifecycle is not initial");
    };
  }

  @Override
  public Lifecycle replace(Details details) {
    return switch (state) {
      case Registered, Open -> this;
      default -> throw new IllegalStateException("Can't apply replace, lifecycle is not in its boundaries");
    };
  }

  @Override
  public Lifecycle open() {
    return switch (state) {
      case Registered -> new Staging(Open);
      default -> throw new IllegalStateException("Can't apply open, lifecycle is not registered");
    };
  }

  @Override
  public Lifecycle close() {
    return switch (state) {
      case Open -> new Staging(Closed);
      default -> throw new IllegalStateException("Can't apply close, lifecycle is not open");
    };
  }

  @Override
  public Lifecycle archive() {
    return switch (state) {
      case Closed -> new Staging(Archived);
      default -> throw new IllegalStateException("Can't apply archive, lifecycle is not closed");
    };
  }

  @Override
  public Lifecycle reopen() {
    return switch (state) {
      case Closed -> new Staging(Open);
      default -> throw new IllegalStateException("Can't apply reopen, lifecycle is not closed");
    };
  }
}
