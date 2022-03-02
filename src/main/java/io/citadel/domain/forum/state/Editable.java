package io.citadel.domain.forum.state;

import io.citadel.domain.forum.Forum;
import io.citadel.shared.domain.Domain;

public sealed interface Editable extends Domain.Aggregate<Forum> permits Forum {
  default Forum edit(Forum.Name name) {
    return switch (this) {
      case States.Registered it -> new States.Registered(
        it.id(),
        it.version(),
        it.model().name(name),
        it.events().push(Forum.events.edited(name))
      );
      case States.Open it -> new States.Open(
        it.id(),
        it.version(),
        it.model().name(name),
        it.events().push(Forum.events.edited(name))
      );
      default -> throw new IllegalStateException("Unexpected value: " + this);
    };
  }

  default Forum edit(Forum.Description description) {
    return switch (this) {
      case States.Registered it -> new States.Registered(
        it.id(),
        it.version(),
        it.model().description(description),
        it.events().push(Forum.events.edited(description))
      );
      case States.Open it -> new States.Open(
        it.id(),
        it.version(),
        it.model().description(description),
        it.events().push(Forum.events.edited(description))
      );
      default -> throw new IllegalStateException("Unexpected value: " + this);
    };
  }
}
