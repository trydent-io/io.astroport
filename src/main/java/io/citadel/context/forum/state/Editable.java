package io.citadel.context.forum.state;

import io.citadel.context.forum.Forum;

import static io.citadel.context.forum.Forum.State.Open;
import static io.citadel.context.forum.Forum.State.Registered;

public sealed interface Editable permits Forum {
  default Forum edit(Forum.Name name) {
    return switch (this) {
      case States.Aggregate it && it.is(Registered) -> Forum.states.registered(
        it.id(),
        it.version(),
        it.model().name(name),
        it.events().push(Forum.event.edited(name))
      );
      case States.Aggregate it && it.is(Open) -> Forum.states.open(
        it.id(),
        it.version(),
        it.model().name(name),
        it.events().push(Forum.event.edited(name))
      );
      default -> throw new IllegalStateException("Unexpected value: " + this);
    };
  }

  default Forum edit(Forum.Description description) {
    return switch (this) {
      case States.Aggregate it && it.is(Registered) -> Forum.states.registered(
        it.id(),
        it.version(),
        it.model().description(description),
        it.events().push(Forum.event.edited(description))
      );
      case States.Aggregate it && it.is(Open) -> Forum.states.open(
        it.id(),
        it.version(),
        it.model().description(description),
        it.events().push(Forum.event.edited(description))
      );
      default -> throw new IllegalStateException("Unexpected value: " + this);
    };
  }
}
