package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.func.Maybe;
import io.citadel.kernel.func.ThrowableBiFunction;
import io.vertx.core.Future;

import java.util.stream.Stream;

public sealed interface Aggregate extends Forum<Aggregate>, Domain.Aggregate permits Aggregate.Root {
  static Aggregate root(Model model, long version) {
    return new Root(model, version, new Transaction(new Lifecycle()));
  }

  record Root(Model model, long version, Transaction transaction) implements Aggregate {
    @Override
    public Future<Aggregate> register(Details details) {
      return transaction.register(details).map(it -> new Root(model, version, it));
    }

    @Override
    public Future<Aggregate> replace(Details details) {
      return transaction.replace(details).map(it -> new Root(model, version, it));
    }

    @Override
    public Future<Aggregate> open() {
      return transaction.open().map(it -> new Root(model, version, it));
    }

    @Override
    public Future<Aggregate> close() {
      return transaction.close().map(it -> new Root(model, version, it));
    }

    @Override
    public Future<Aggregate> archive() {
      return transaction.archive().map(it -> new Root(model, version, it));
    }

    @Override
    public Future<Aggregate> reopen() {
      return transaction.reopen().map(it -> new Root(model, version, it));
    }

    @Override
    public <T> T commit() {
      return null;
    }
  }
}

