package io.citadel.context.forum;

import io.citadel.context.forum.repository.Repository;
import io.citadel.eventstore.EventStore;
import io.citadel.shared.context.Domain;

public sealed interface Forums extends Domain.Repository<Forum, Forum.ID, Model> permits Repository {
  static Forums repository(EventStore eventStore, Forum.Hydration hydration) {
    return new Repository(eventStore, hydration);
  }
}
