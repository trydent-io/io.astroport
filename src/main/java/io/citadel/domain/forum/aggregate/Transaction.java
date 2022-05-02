package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.func.Maybe;
import io.citadel.kernel.func.ThrowableFunction;
import io.vertx.core.Future;

import java.util.Optional;
import java.util.stream.Stream;

public record Transaction(Lifecycle lifecycle, Stream<Forum.Event> events) implements Forum<Transaction>, Domain.Transaction {
  public Transaction(Lifecycle lifecycle) { this(lifecycle, Stream.empty()); }

  private Stream<Event> append(Event... events) {
    return this.events != null
      ? Stream.concat(this.events, Stream.of(events))
      : Stream.of(events);
  }

  @Override
  public Future<Transaction> register(Details details) {
    return lifecycle.register(details).map(it -> new Transaction(lifecycle, append(Forum.events.registered(details))));
  }

  @Override
  public Future<Transaction> replace(Details details) {
    return lifecycle.replace(details).map(it -> new Transaction(lifecycle, append(Forum.events.replaced(details))));
  }

  @Override
  public Future<Transaction> open() {
    return lifecycle.open().map(it -> new Transaction(lifecycle, append(Forum.events.opened())));
  }

  @Override
  public Future<Transaction> close() {
    return lifecycle.close().map(it -> new Transaction(lifecycle, append(Forum.events.closed())));
  }

  @Override
  public Future<Transaction> archive() {
    return lifecycle.archive().map(it -> new Transaction(lifecycle, append(Forum.events.archived())));
  }

  @Override
  public Future<Transaction> reopen() {
    return lifecycle.reopen().map(it -> new Transaction(lifecycle, append(Forum.events.reopened())));
  }

  @Override
  public Future<Void> commit(String aggregateId, String aggregateName, long aggregateVersion, String by) {
    return null;
  }
}
