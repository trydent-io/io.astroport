package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.func.Maybe;
import io.citadel.kernel.func.ThrowableFunction;

import java.util.Optional;
import java.util.stream.Stream;

public record Transaction(Lifecycle lifecycle, Stream<Forum.Event> events) implements Forum<Transaction>, Domain.Seed<Stream<Forum.Event>> {
  public Transaction(Lifecycle lifecycle) { this(lifecycle, Stream.empty()); }

  private Stream<Event> append(Event... events) {
    return Optional.ofNullable(this.events)
      .map(it -> Stream.concat(it, Stream.of(events)))
      .orElseGet(() -> Stream.of(events));
  }

  @Override
  public Maybe<Transaction> register(Name name, Description description) {
    return lifecycle.register(name, description).map(() -> new Transaction(lifecycle, append(Forum.events.registered(name, description))));
  }

  @Override
  public Maybe<Transaction> replace(Name name, Description description) {
    return lifecycle.replace(name, description).map(() -> new Transaction(lifecycle, append(Forum.events.replaced(name, description))));
  }

  @Override
  public Maybe<Transaction> open() {
    return lifecycle.open().map(() -> new Transaction(lifecycle, append(Forum.events.opened())));
  }

  @Override
  public Maybe<Transaction> close() {
    return lifecycle.close().map(() -> new Transaction(lifecycle, append(Forum.events.closed())));
  }

  @Override
  public Maybe<Transaction> archive() {
    return lifecycle.archive().map(() -> new Transaction(lifecycle, append(Forum.events.archived())));
  }

  @Override
  public Maybe<Transaction> reopen() {
    return lifecycle.reopen().map(() -> new Transaction(lifecycle, append(Forum.events.reopened())));
  }

  @Override
  public <R> R eventually(ThrowableFunction<? super Stream<Event>, ? extends R> then) {
    return then.apply(events);
  }
}
