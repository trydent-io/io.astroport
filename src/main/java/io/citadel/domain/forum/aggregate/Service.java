package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.kernel.func.ThrowableFunction;

record Service<F extends Forum<F>>(State state, F forum) implements Lifecycle<F> {
  @Override
  public Lifecycle<F> register(final Name name, final Description description) {
    return switch (state) {
      case null -> new Service<>(State.Registered, forum.register(name, description));
      default -> null;
    };
  }

  @Override
  public Lifecycle<F> change(Name name, Description description) {
    return switch (state) {
      case Registered, Open -> new Service<>(state, forum.change(name, description));
      default -> null;
    };
  }

  @Override
  public Lifecycle<F> open() {
    return switch (state) {
      case Registered -> new Service<>(State.Open, forum.open());
      default -> null;
    };
  }

  @Override
  public Lifecycle<F> close() {
    return switch (state) {
      case Open -> new Service<>(State.Closed, forum.close());
      default -> null;
    };
  }

  @Override
  public Lifecycle<F> archive() {
    return switch (state) {
      case Closed -> new Service<>(State.Archived, forum.archive());
      default -> null;
    };
  }

  @Override
  public Lifecycle<F> reopen() {
    return switch (state) {
      case Closed -> new Service<>(State.Open, forum.reopen());
      default -> null;
    };
  }

  @Override
  public <R> R eventually(final ThrowableFunction<? super F, ? extends R> then) {
    return then.apply(forum);
  }
}
