package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.kernel.eventstore.EventStore;

public enum Defaults {
  Companion;

  public Forums forums(EventStore eventStore) {
    return Forums.repository(eventStore, Snapshot.hydration());
  }

  public Forum.Model model(String id) {
    return new Forum.Model(Forum.attributes.id(id));
  }
}
