package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.kernel.domain.Domain;
import io.vertx.core.Future;

public record Root(Model model, long version, Lifecycle lifecycle, Domain.Transaction transaction) implements Forum.Aggregate {
  @Override
  public Future<Aggregate> register(Details details) {
    return lifecycle
      .register(details)
      .map(it ->
        new Root(
          model,
          version,
          lifecycle,
          transaction.log(Forum.events.registered(details))
        )
      );
  }

  @Override
  public Future<Aggregate> replace(Details details) {
    return lifecycle
      .replace(details)
      .map(it ->
        new Root(
          model,
          version,
          lifecycle,
          transaction.log(Forum.events.replaced(details))
        )
      );
  }

  @Override
  public Future<Aggregate> open() {
    return lifecycle
      .open()
      .map(it ->
        new Root(
          model,
          version,
          lifecycle,
          transaction.log(Forum.events.opened())
        )
      );
  }

  @Override
  public Future<Aggregate> close() {
    return lifecycle
      .close()
      .map(it ->
        new Root(
          model,
          version,
          lifecycle,
          transaction.log(Forum.events.closed())
        )
      );
  }

  @Override
  public Future<Aggregate> archive() {
    return lifecycle
      .archive()
      .map(it ->
        new Root(
          model,
          version,
          lifecycle,
          transaction.log(Forum.events.archived())
        )
      );
  }

  @Override
  public Future<Aggregate> reopen() {
    return lifecycle
      .reopen()
      .map(it ->
        new Root(
          model,
          version,
          lifecycle,
          transaction.log(Forum.events.reopened())
        )
      );
  }

  @Override
  public Future<Void> submit(final String by) {
    return transaction.commit(model.id().toString(), "FORUM", version, by);
  }
}
