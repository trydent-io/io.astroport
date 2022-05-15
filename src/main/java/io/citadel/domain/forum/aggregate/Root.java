package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.kernel.domain.Domain;
import io.vertx.core.Future;

public record Root(Model model, long version, Lifecycle lifecycle, Domain.Transaction transaction) implements Forum.Aggregate {
  @Override
  public Aggregate register(Details details) {
    lifecycle.register(details);
    return new Root(
      model,
      version,
      lifecycle,
      transaction.log(Forum.events.registered(details))
    );
  }

  @Override
  public Aggregate replace(Details details) {
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
  public Aggregate open() {
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
  public Aggregate close() {
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
  public Aggregate archive() {
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
  public Aggregate reopen() {
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
