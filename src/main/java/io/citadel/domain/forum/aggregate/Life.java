package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.func.ThrowableFunction;

public sealed interface Life<F extends Forum<F>> extends Forum<Life<F>>, Domain.Life<F> permits Life.Type.Span {
  static <F extends Forum<F>> Life<F> span(F forum) {
    return new Life.Type.Span<>(null, forum);
  }

  enum Type {;
    private record Span<F extends Forum<F>>(Forum.State state, F forum) implements Life<F> {
      @Override
      public <R> R eventually(final ThrowableFunction<? super F, ? extends R> then) {
        return then.apply(forum);
      }

      @Override
      public Life<F> register(final Name name, final Description description) {
        return switch (state) {
          case null -> new Span<>(State.Registered, forum.register(name, description));
          default -> null;
        };
      }

      @Override
      public Life<F> edit(Name name, Description description) {
        return switch (state) {
          case Registered, Open -> new Span<>(state, forum.edit(name, description));
          default -> null;
        };
      }

      @Override
      public Life<F> open() {
        return switch (state) {
          case Registered -> new Span<>(State.Open, forum.open());
          default -> null;
        };
      }

      @Override
      public Life<F> close() {
        return switch (state) {
          case Open -> new Span<>(State.Closed, forum.close());
          default -> null;
        };
      }

      @Override
      public Life<F> archive() {
        return switch (state) {
          case Closed -> new Span<>(State.Archived, forum.archive());
          default -> null;
        };
      }

      @Override
      public Life<F> reopen() {
        return switch (state) {
          case Closed -> new Span<>(State.Open, forum.reopen());
          default -> null;
        };
      }
    }
  }
}
