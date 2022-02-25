package io.citadel.forum.state;

import io.citadel.forum.Actioned;
import io.citadel.forum.Forum;
import io.citadel.kernel.domain.Domain;
import io.citadel.member.MemberID;

import java.time.LocalDateTime;

public sealed interface Closeable extends Domain.Aggregate<Forum> permits Forum {
  default Forum close(LocalDateTime at, MemberID by) {
    return switch (this) {
      case States.Open it -> new States.Closed(
        it.id(),
        it.version(),
        it.state().closed(new Actioned(at, by)),
        it.events().push(Forum.events.closed(at, by))
      );
      default -> throw new IllegalStateException("Unexpected value: " + this);
    };
  }
}
