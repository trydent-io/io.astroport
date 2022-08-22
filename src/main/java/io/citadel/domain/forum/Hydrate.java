package io.citadel.domain.forum;

import io.citadel.kernel.domain.Archetype;
import io.citadel.kernel.eventstore.Audit;

public final class Hydrate implements Archetype<Forum.ID, Forum.Entity, Forum.Event, Forum.State> {
  @Override
  public Forum.State initial() {
    return Forum.State.Registered;
  }

  @Override
  public Forum.Entity initialize(Forum.ID id) {
    return new Forum.Entity(id);
  }

  @Override
  public Forum.Event transform(Audit.Event event) {
    return switch (Forum.Event.Names.valueOf(event.name())) {
      case Opened -> event.data().mapTo(Forum.Event.Opened.class);
      case Closed -> event.data().mapTo(Forum.Event.Closed.class);
      case Registered -> event.data().mapTo(Forum.Event.Registered.class);
      case Reopened -> event.data().mapTo(Forum.Event.Reopened.class);
      case Replaced -> event.data().mapTo(Forum.Event.Replaced.class);
      case Archived -> event.data().mapTo(Forum.Event.Archived.class);
    };
  }

  @Override
  public Forum.Entity accumulate(Forum.Entity entity, Forum.Event event) {
    return switch (event) {
      case Forum.Event.Archived archived -> entity;
      case Forum.Event.Closed closed -> entity;
      case Forum.Event.Opened opened -> entity;
      case Forum.Event.Registered registered -> entity.details(registered.name(), registered.description());
      case Forum.Event.Reopened reopened -> entity;
      case Forum.Event.Replaced replaced -> entity.details(replaced.name(), replaced.description());
    };
  }
}
