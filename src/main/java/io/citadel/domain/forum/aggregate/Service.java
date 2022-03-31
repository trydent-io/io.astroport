package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.func.ThrowableFunction;

public sealed interface Service<F extends Forum<F>> extends Forum<Service<F>>, Domain.Service<F> permits Service.Type.Lifecycle {
  static <F extends Forum<F>> Service<F> lifecycle(F forum) {
    return new Type.Lifecycle<>(null, forum);
  }

  enum Type {;
    private record Lifecycle<F extends Forum<F>>(Forum.State state, F forum) implements Service<F> {
      @Override
      public Service<F> register(final Name name, final Description description) {
        return switch (state) {
          case null -> new Lifecycle<>(State.Registered, forum.register(name, description));
          default -> null;
        };
      }

      @Override
      public Service<F> edit(Name name, Description description) {
        return switch (state) {
          case Registered, Open -> new Lifecycle<>(state, forum.edit(name, description));
          default -> null;
        };
      }

      @Override
      public Service<F> open() {
        return switch (state) {
          case Registered -> new Lifecycle<>(State.Open, forum.open());
          default -> null;
        };
      }

      @Override
      public Service<F> close() {
        return switch (state) {
          case Open -> new Lifecycle<>(State.Closed, forum.close());
          default -> null;
        };
      }

      @Override
      public Service<F> archive() {
        return switch (state) {
          case Closed -> new Lifecycle<>(State.Archived, forum.archive());
          default -> null;
        };
      }

      @Override
      public Service<F> reopen() {
        return switch (state) {
          case Closed -> new Lifecycle<>(State.Open, forum.reopen());
          default -> null;
        };
      }

      @Override
      public <R> R eventually(final ThrowableFunction<? super F, ? extends R> then) {
        return then.apply(forum);
      }
    }
  }
}
