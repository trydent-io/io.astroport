package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;

public enum Defaults {
  Companion;

  public Snapshot snapshot(Forum.ID id) {
    return Snapshot.timepoint(Lifecycle.service(Snapshot.hydration(id)));
  }

  public Aggregate aggregate(Forum.ID id, long version) {
    return Aggregate.transaction(Lifecycle.service(Aggregate.root(id, version)));
  }

  public Aggregate aggregate(Forum.Model model, long version) {
    return Aggregate.transaction(Lifecycle.service(Aggregate.root(model, version)));
  }
}
