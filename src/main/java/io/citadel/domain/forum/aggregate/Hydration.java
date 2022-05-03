package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.forum.handler.Events;
import io.citadel.kernel.domain.Domain;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import static io.citadel.domain.forum.handler.Events.Names.valueOf;

public record Hydration(Lifecycle lifecycle, Model model, long version) implements Forum.Snapshot {
  Hydration(Lifecycle lifecycle) {this(lifecycle, null, -1);}

  @Override
  public Domain.Snapshot<Aggregate, Model> apply(String aggregateId, long aggregateVersion, String eventName, JsonObject eventData) {
    final var snapshot = model == null ? new Hydration(lifecycle, Forum.defaults.model(aggregateId), version) : this;
    return
      (
        switch (valueOf(eventName)) {
          case Opened ->
            snapshot.open();
          case Closed ->
            snapshot.close();
          case Registered ->
            snapshot.register(eventData.mapTo(Events.Registered.class).details());
          case Reopened ->
            snapshot.reopen();
          case Replaced ->
            snapshot.replace(eventData.mapTo(Events.Replaced.class).details());
          case Archived ->
            snapshot.archive();
        }
      ).otherwise("Can't hydrate aggregate for Forum with ID %s".formatted(aggregateId), IllegalStateException::new);
  }

  @Override
  public Aggregate aggregate(final Domain.Transaction transaction) {
    return new Root(model, version, lifecycle, transaction);
  }

  @Override
  public Future<Snapshot> register(final Details details) {
    return lifecycle.register(details).map(it -> new Hydration(it, new Model(model.id(), details), version));
  }

  @Override
  public Future<Snapshot> replace(final Details details) {
    return lifecycle.replace(details).map(it -> new Hydration(it, new Model(model.id(), details), version));
  }

  @Override
  public Future<Snapshot> open() {
    return lifecycle.open().map(it -> new Hydration(it, model, version));
  }

  @Override
  public Future<Snapshot> close() {
    return lifecycle.close().map(it -> new Hydration(it, model, version));
  }

  @Override
  public Future<Snapshot> archive() {
    return lifecycle.archive().map(it -> new Hydration(it, model, version));
  }

  @Override
  public Future<Snapshot> reopen() {
    return lifecycle.reopen().map(it -> new Hydration(it, model, version));
  }
}
