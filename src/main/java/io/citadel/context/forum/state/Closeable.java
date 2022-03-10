package io.citadel.context.forum.state;

import io.citadel.context.forum.Forum;
import io.citadel.context.member.Member;

import static io.citadel.context.forum.Forum.State.Open;

public sealed interface Closeable permits Forum {
  default Forum close(Member.ID by) {
    return switch (this) {
      case States.Aggregate it && it.is(Open) -> Forum.states.close(
        it.id(),
        it.version(),
        it.model().closed(by),
        it.events().push(Forum.events.closed(by))
      );
      default -> throw new IllegalStateException("Unexpected value: " + this);
    };
  }
}
