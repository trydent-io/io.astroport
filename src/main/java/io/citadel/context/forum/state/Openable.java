package io.citadel.context.forum.state;

import io.citadel.context.forum.Forum;
import io.citadel.context.member.Member;

import java.util.stream.Stream;

import static io.citadel.context.forum.Forum.State.Closed;
import static io.citadel.context.forum.Forum.State.Registered;

public sealed interface Openable permits Forum {
  default Forum open(Member.ID by) {
    return switch (this) {
      case States.Aggregate aggregate && aggregate.is(Registered) -> Forum.states.open(
        aggregate.id(),
        aggregate.version(),
        aggregate.model().opened(by),
        Stream.concat(aggregate.events(), Stream.of(Forum.event.opened(by)))
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
        it.events().push(Forum.event.reopened(by))
      );
      default -> throw new IllegalStateException("Unexpected values: " + this);
    };
  }
}
