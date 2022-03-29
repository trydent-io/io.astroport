package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.eventstore.data.MetaAggregate;
import io.citadel.eventstore.data.MetaEvent;
import io.citadel.kernel.domain.Eventable;
import io.citadel.kernel.func.ThrowableBiFunction;
import io.vertx.core.Future;

import java.util.stream.Stream;

public record Providing(Model model, long version, Stream<Event> events) implements Forum.Transaction, Eventable<Forum.Event> {
  @Override
  public Forum register(final Name name, Description description) {
    return new Providing(model, version, append(events, Forum.event.registered(name, description, null)));
  }

  @Override
  public Forum open() {
    return new Providing(model, version, append(events, Forum.event.opened(null)));
  }

  @Override
  public Forum close() {
    return new Providing(model, version, append(events, Forum.event.closed(null)));
  }

  @Override
  public Forum archive() {
    return new Providing(model, version, append(events, Forum.event.archived(null)));
  }

  @Override
  public Forum reopen() {
    return new Providing(model, version, append(events, Forum.event.reopened(null)));
  }

  @Override
  public Forum edit(final Name name, Description description) {
    return new Providing(model, version, append(events, Forum.event.edited(name, description)));
  }

  @Override
  public Future<Void> commit(final ThrowableBiFunction<? super MetaAggregate, ? super Stream<MetaEvent>, ? extends Future<Void>> transaction) {
    return transaction.apply(null, null);
  }
}
