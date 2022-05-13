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
  public Snapshot register(final Details details) {
    return new Hydration(lifecycle.register(details), new Model(modelled.id(), details), version);
  }

  @Override
  public Snapshot replace(final Details details) {
    return new Hydration(lifecycle.replace(details), new Model(modelled.id(), details), version);
  }

  @Override
  public Snapshot open() {
    return new Hydration(lifecycle.open(), modelled, version);
  }

  @Override
  public Snapshot close() {
    return new Hydration(lifecycle.close(), modelled, version);
  }

  @Override
  public Snapshot archive() {
    return new Hydration(lifecycle.archive(), modelled, version);
  }

  @Override
  public Snapshot reopen() {
    return new Hydration(lifecycle.reopen(), modelled, version);
  }
}
