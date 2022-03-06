package io.citadel.context.forum.state;

import io.citadel.context.forum.Actioned;
import io.citadel.context.forum.Forum;
import io.citadel.shared.domain.Domain;
import io.citadel.context.member.Member;

import java.time.LocalDateTime;

public sealed interface Closeable permits Forum {
  default Forum close(LocalDateTime at, Member.ID by) {
    return switch (this) {
      case States.Open it -> new States.Closed(
        it.id(),
        it.version(),
        it.model().closed(new Actioned(at, by)),
        it.events().push(Forum.events.closed(at, by))
      );
      default -> throw new IllegalStateException("Unexpected value: " + this);
    };
  }
}
