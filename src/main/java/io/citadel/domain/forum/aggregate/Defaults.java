package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.eventstore.EventStore;

public enum Defaults {
  Companion;

  public Forums forums(EventStore eventStore) {
    return new Repository(Domain.defaults.repository(eventStore, Forum.defaults.snapshot(), Forum.AGGREGATE_NAME));
  }

  public Forum.Model model(String id) {
    return new Forum.Model(Forum.attributes.id(id));
  }

  public Forum.Snapshot snapshot() {
    return new Hydration(new Staging());
  }

  public Forum.Aggregate aggregate(Forum.Model model, long version, Forum.Lifecycle lifecycle, final Domain.Transaction transaction) {
    return new Root(model, version, lifecycle, transaction);
  }
}
