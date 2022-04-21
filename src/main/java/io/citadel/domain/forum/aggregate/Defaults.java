package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.kernel.eventstore.EventStore;

public enum Defaults {
  Companion;

  public Forums forums(EventStore eventStore) {
    return Forums.repository(eventStore, snapshot());
  }

  public Forum.Model model(String id) {
    return new Forum.Model(new Forum.ID(id));
  }

  public Snapshot snapshot() {
    return Snapshot.timepoint(Seed.service(Snapshot.hydration()));
  }

  public Aggregate aggregate(Forum.ID id, long version) {
    return Aggregate.transaction(Seed.service(Aggregate.root(id, version)));
  }

  public Aggregate aggregate(Forum.Model model, long version) {
    return Aggregate.transaction(Seed.service(Aggregate.root(model, version)));
  }

  public Snap identity(final String id) {
    return new Snap(new Lifecycle(), new Forum.Model(new Forum.ID(id)));
  }
}
