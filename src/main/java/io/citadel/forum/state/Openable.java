package io.citadel.forum.state;

import io.citadel.forum.Actioned;
import io.citadel.forum.Forum;
import io.citadel.kernel.domain.Domain;
import io.citadel.member.MemberID;

import java.time.LocalDateTime;

public sealed interface Openable extends Domain.Aggregate<Forum> permits Forum {
  default Forum open(LocalDateTime at, MemberID by) {
    return switch (this) {
      case States.Registered it -> new States.Open(
        it.id(),
        it.version(),
        it.state().opened(new Actioned(at, by)),
        it.events().push(Forum.events.opened(at, by))
      );
      default -> throw new IllegalStateException("Unexpected value: " + this);
    };
  }
}
