package io.citadel.domain.forum.aggregate;

import io.citadel.CitadelException;
import io.citadel.domain.forum.Forum;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.func.Maybe;
import io.citadel.kernel.func.ThrowableFunction;

public record Lifecycle(State state) implements Forum<Lifecycle>, Domain.Seed<Forum.State> {
  public Lifecycle() {this(null);}

  @Override
  public Maybe<Lifecycle> register(Name name, Description description) {
    return switch (state) {
      case null ->
        Maybe.of(new Lifecycle(State.Registered));
      default ->
        Maybe.error(new CitadelException("Can't apply register, lifecycle is identity"));
    };
  }

  @Override
  public Maybe<Lifecycle> replace(Name name, Description description) {
    return switch (state) {
      case Registered, Open ->
        Maybe.of(this);
      default ->
        Maybe.error(new CitadelException("Can't apply replace, lifecycle is inconsistent"));
    };
  }

  @Override
  public Maybe<Lifecycle> open() {
    return switch (state) {
      case Registered -> Maybe.of(new Lifecycle(State.Open));
      default -> Maybe.error(new CitadelException("Can't apply open, lifecycle is not registered"));
    };
  }

  @Override
  public Maybe<Lifecycle> close() {
    return switch (state) {
      case Open ->
        Maybe.of(new Lifecycle(State.Closed));
      default ->
        Maybe.error(new CitadelException("Can't apply close, lifecycle is not open"));
    };
  }

  @Override
  public Maybe<Lifecycle> archive() {
    return switch (state) {
      case Closed ->
        Maybe.of(new Lifecycle(State.Archived));
      default ->
        Maybe.error(new CitadelException("Can't apply archive, lifecycle is not closed"));
    };
  }

  @Override
  public Maybe<Lifecycle> reopen() {
    return switch (state) {
      case Closed ->
        Maybe.of(new Lifecycle(State.Open));
      default ->
        Maybe.error(new CitadelException("Can't apply reopen, lifecycle is not closed"));
    };
  }

  @Override
  public <R> R eventually(ThrowableFunction<? super State, ? extends R> then) {
    return then.apply(state);
  }
}
