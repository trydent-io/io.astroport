package io.citadel.context.forum.state;

import io.citadel.context.forum.Actioned;
import io.citadel.context.forum.Forum;
import io.citadel.context.member.Member;

import java.time.LocalDateTime;

import static io.citadel.context.forum.Forum.State.Closed;
import static io.citadel.context.forum.Forum.State.Registered;

public sealed interface Openable permits Forum {
  default Forum open(Member.ID by) {
    return switch (this) {
      case States.Aggregate it && it.is(Registered) -> Forum.states.open(
        it.id(),
        it.version(),
        it.model().opened(by),
        it.events().push(Forum.events.opened(by))
      );
      default -> throw new IllegalStateException("Unexpected value: " + this);
    };
  }

  default Forum reopen(Member.ID by) {
    return switch (this) {
      case States.Aggregate it && it.is(Closed) -> Forum.states.open(
        it.id(),
        it.version(),
        it.model().reopened(by),
        it.events().push(Forum.events.reopened(by))
      );
      default -> throw new IllegalStateException("Unexpected values: " + this);
    };
  }
}
