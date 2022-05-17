package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.forum.handler.Events;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.eventstore.EventStore;
import io.vertx.core.json.JsonObject;

import java.util.function.Predicate;

import static io.citadel.domain.forum.handler.Events.Names.valueOf;

public record Snapshot(Lifecycle lifecycle, Model modelled, long version) implements Forum<Snapshot>, Domain.Snapshot<Forum.Model, Forum.Aggregate> {
  Snapshot(Lifecycle lifecycle) {this(lifecycle, null, -1);}

  @Override
  public Snapshot apply(String id, long version, String eventName, JsonObject eventData) {
    return switch (valueOf(eventName)) {
      case Opened -> snapshot(id, version).open();
      case Closed -> snapshot(id, version).close();
      case Registered -> snapshot(id, version).register(eventData.mapTo(Events.Registered.class).details());
      case Reopened -> snapshot(id, version).reopen();
      case Replaced -> snapshot(id, version).replace(eventData.mapTo(Events.Replaced.class).details());
      case Archived -> snapshot(id, version).archive();
    };
  }

  private Snapshot snapshot(final String id, long version) {
    return modelled == null
      ? new Snapshot(lifecycle, Forum.defaults.model(id), version)
      : this;
  }

  @Override
  public Aggregate aggregate(final EventStore eventStore) {
    return Forum.defaults.aggregate(modelled, version, lifecycle, Domain.defaults.transaction(eventStore));
  }

  @Override
  public Aggregate aggregate(EventStore eventStore, Predicate<? super Model> verify) {
    return verify.test(modelled) ? new Root(modelled, version, lifecycle, Domain.defaults.transaction(eventStore)) : null;
  }

  @Override
  public Snapshot register(final Details details) {
    return new Snapshot(lifecycle.register(details), new Model(modelled.id(), details), version);
  }

  @Override
  public Snapshot replace(final Details details) {
    return new Snapshot(lifecycle.replace(details), new Model(modelled.id(), details), version);
  }

  @Override
  public Snapshot open() {
    return new Snapshot(lifecycle.open(), modelled, version);
  }

  @Override
  public Snapshot close() {
    return new Snapshot(lifecycle.close(), modelled, version);
  }

  @Override
  public Snapshot archive() {
    return new Snapshot(lifecycle.archive(), modelled, version);
  }

  @Override
  public Snapshot reopen() {
    return new Snapshot(lifecycle.reopen(), modelled, version);
  }
}
