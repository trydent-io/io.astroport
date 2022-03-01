package io.citadel.domain.forum.state;

import io.citadel.domain.forum.Actioned;
import io.citadel.domain.forum.Forum;
import io.citadel.shared.domain.Domain;
import io.citadel.domain.member.MemberID;

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
