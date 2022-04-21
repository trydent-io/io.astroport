package io.citadel.domain.forum.aggregate;

import io.citadel.kernel.func.Maybe;
import io.citadel.kernel.func.ThrowableBiFunction;

import java.util.stream.Stream;

record Root(Model model, long version, Transaction transaction) implements Aggregate {
  @Override
  public Maybe<Aggregate> register(final Name name, final Description description) {
    return transaction.register(name, description).map(it -> new Root(model, version, it));
  }

  @Override
  public Maybe<Aggregate> replace(final Name name, final Description description) {
    return transaction.replace(name, description).map(it -> new Root(model, version, it));
  }

  @Override
  public Maybe<Aggregate> open() {
    return transaction.open().map(it -> new Root(model, version, it));
  }

  @Override
  public Maybe<Aggregate> close() {
    return transaction.close().map(it -> new Root(model, version, it));
  }

  @Override
  public Maybe<Aggregate> archive() {
    return transaction.archive().map(it -> new Root(model, version, it));
  }

  @Override
  public Maybe<Aggregate> reopen() {
    return transaction.reopen().map(it -> new Root(model, version, it));
  }

  @Override
  public <T> T commit(final ThrowableBiFunction<? super AggregateInfo, ? super Stream<EventInfo>, ? extends T> transaction) {
    return null;
  }
}
