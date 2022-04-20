package io.citadel.domain.forum.aggregate;

import io.citadel.CitadelException;
import io.citadel.domain.forum.Forum;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.func.Maybe;
import io.citadel.kernel.func.ThrowableFunction;

import java.util.stream.Stream;

public sealed interface Lifecycle<T> extends Forum<Lifecycle<T>>, Domain.Lifecycle<T> permits Lifecycle.Type.Snapshot, Lifecycle.Type.Span, Lifecycle.Type.Transaction, Service {
  static Lifecycle<Snapshot> identity(Forum.ID id) {
    return new Type.Snapshot(
      new Type.Span(),
      new Model(id)
    );
  }

  static Lifecycle<Stream<Forum.Event>> transaction(Forum.State state, Forum.Event... events) {
    return new Type.Transaction(
      new Type.Span(state),
      Stream.of(events)
    );
  }
  record Snapshot(Forum.State state, Forum.Model model) {}

  static <F extends Forum<F>> Lifecycle<F> service(F forum) {
    return new Service<>(null, forum);
  }

  enum Type {;
    private record Span(State state) implements Lifecycle<State> {
      private Span() {this(null);}

      @Override
      public Maybe<Lifecycle<State>> register(Name name, Description description) {
        return switch (state) {
          case null -> Maybe.of(new Span(State.Registered));
          default -> Maybe.error(new CitadelException("Can't apply register, lifecycle is identity"));
        };
      }

      @Override
      public Maybe<Lifecycle<State>> replace(Name name, Description description) {
        return switch (state) {
          case Registered, Open -> Maybe.of(this);
          default -> Maybe.error(new CitadelException("Can't apply replace, lifecycle is inconsistent"));
        };
      }

      @Override
      public Maybe<Lifecycle<State>> open() {
        return switch (state) {
          case Registered -> Maybe.of(new Span(State.Open));
          default -> Maybe.error(new CitadelException("Can't apply open, lifecycle is not registered"));
        };
      }

      @Override
      public Maybe<Lifecycle<State>> close() {
        return switch (state) {
          case Open -> Maybe.of(new Span(State.Closed));
          default -> Maybe.error(new CitadelException("Can't apply close, state is not open"));
        };
      }

      @Override
      public Maybe<Lifecycle<State>> archive() {
        return switch (state) {
          case Closed -> Maybe.of(new Span(State.Archived));
          default -> Maybe.error(new CitadelException("Can't apply archive, state is not closed"));
        };
      }

      @Override
      public Maybe<Lifecycle<State>> reopen() {
        return switch (state) {
          case Closed -> Maybe.of(new Span(State.Open));
          default -> Maybe.error(new CitadelException("Can't apply reopen, state is not closed"));
        };
      }

      @Override
      public <R> R eventually(ThrowableFunction<? super State, ? extends R> then) {
        return then.apply(state);
      }
    }

    private record Snapshot(Lifecycle<State> state, Forum.Model model) implements Lifecycle<Lifecycle.Snapshot> {
      @Override
      public Maybe<Lifecycle<Snapshot>> register(Name name, Description description) {
        return state.register(name, description).map(it -> new Type.Snapshot(it, new Model(model.id(), new Details(name, description))));
      }

      @Override
      public Maybe<Lifecycle<Snapshot>> replace(Name name, Description description) {
        return state.replace(name, description).map(it -> new Type.Snapshot(it, new Model(model.id(), new Details(name, description))));
      }

      @Override
      public Maybe<Lifecycle<Snapshot>> open() {
        return state.open().map(it -> new Type.Snapshot(it, model));
      }

      @Override
      public Maybe<Lifecycle<Snapshot>> close() {
        return state.close().map(it -> new Type.Snapshot(it, model));
      }

      @Override
      public Maybe<Lifecycle<Snapshot>> archive() {
        return state.archive().map(it -> new Type.Snapshot(it, model));
      }

      @Override
      public Maybe<Lifecycle<Snapshot>> reopen() {
        return state.reopen().map(it -> new Type.Snapshot(it, model));
      }

      @Override
      public <R> R eventually(ThrowableFunction<? super Snapshot, ? extends R> then) {
        return state.eventually(it -> then.apply(new Snapshot(it, model)));
      }
    }

    private record Transaction(Lifecycle<State> state, Stream<Event> events) implements Lifecycle<Stream<Forum.Event>> {
      private Stream<Event> append(Forum.Event... events) {
        return this.events != null ? Stream.concat(this.events, Stream.of(events)) : Stream.of(events);
      }

      @Override
      public Maybe<Lifecycle<Stream<Event>>> register(Name name, Description description) {
        return state.register(name, description).map(() -> new Type.Transaction(state, append(Forum.events.registered(name, description))));
      }

      @Override
      public Maybe<Lifecycle<Stream<Event>>> replace(Name name, Description description) {
        return state.replace(name, description).map(() -> new Type.Transaction(state, append(Forum.events.replaced(name, description))));
      }

      @Override
      public Maybe<Lifecycle<Stream<Event>>> open() {
        return state.open().map(() -> new Type.Transaction(state, append(Forum.events.opened())));
      }

      @Override
      public Maybe<Lifecycle<Stream<Event>>> close() {
        return state.close().map(() -> new Type.Transaction(state, append(Forum.events.closed())));
      }

      @Override
      public Maybe<Lifecycle<Stream<Event>>> archive() {
        return state.archive().map(() -> new Type.Transaction(state, append(Forum.events.archived())));
      }

      @Override
      public Maybe<Lifecycle<Stream<Event>>> reopen() {
        return state.reopen().map(() -> new Type.Transaction(state, append(Forum.events.reopened())));
      }

      @Override
      public <R> R eventually(ThrowableFunction<? super Stream<Event>, ? extends R> then) {
        return then.apply(events);
      }
    }
  }
}
