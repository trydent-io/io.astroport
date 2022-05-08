package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.forum.handler.Events;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.eventstore.EventStore;
import io.citadel.kernel.vertx.Task;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.UUID;

import static io.citadel.domain.forum.handler.Events.Names.valueOf;

public record Hydration(Lifecycle lifecycle, Forum.Model modelled, long version) implements Forum.Snapshot, Task {
  Hydration(Lifecycle lifecycle) {this(lifecycle, null, -1);}

  @Override
  public Future<Snapshot> apply(String id, long version, String eventName, JsonObject eventData) {
    return switch (valueOf(eventName)) {
      case Opened -> snapshot(id, version).open();
      case Closed -> snapshot(id, version).close();
      case Registered -> snapshot(id, version).register(eventData.mapTo(Events.Registered.class).details());
      case Reopened -> snapshot(id, version).reopen();
      case Replaced -> snapshot(id, version).replace(eventData.mapTo(Events.Replaced.class).details());
      case Archived -> snapshot(id, version).archive();
    };
  }

  private Hydration snapshot(final String id, long version) {
    return modelled == null
      ? new Hydration(lifecycle, Forum.defaults.model(id), version)
      : this;
  }

  @Override
  public Future<Model> model() {
    return success(modelled);
  }

  @Override
  public Future<Aggregate> aggregate(final EventStore eventStore) {
    return success(Forum.defaults.aggregate(modelled, version, lifecycle, Domain.defaults.transaction(eventStore)));
  }

  @Override
  public Future<Snapshot> register(final Details details) {
    return lifecycle.register(details).map(it -> new Hydration(it, new Model(modelled.id(), details), version));
  }

  @Override
  public Future<Snapshot> replace(final Details details) {
    return lifecycle.replace(details).map(it -> new Hydration(it, new Model(modelled.id(), details), version));
  }

  @Override
  public Future<Snapshot> open() {
    return lifecycle.open().map(it -> new Hydration(it, modelled, version));
  }

  @Override
  public Future<Snapshot> close() {
    return lifecycle.close().map(it -> new Hydration(it, modelled, version));
  }

  @Override
  public Future<Snapshot> archive() {
    return lifecycle.archive().map(it -> new Hydration(it, modelled, version));
  }

  @Override
  public Future<Snapshot> reopen() {
    return lifecycle.reopen().map(it -> new Hydration(it, modelled, version));
  }
}
