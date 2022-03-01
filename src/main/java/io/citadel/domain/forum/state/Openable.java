package io.citadel.domain.forum.state;

import io.citadel.domain.forum.Actioned;
import io.citadel.domain.forum.Forum;
import io.citadel.shared.domain.Domain;
import io.citadel.domain.member.MemberID;

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

  default Forum reopen(LocalDateTime at, MemberID by) {
    return switch (this) {
      case States.Closed it -> new States.Open(
        it.id(),
        it.version(),
        it.state().reopened(new Actioned(at, by)),
        it.events().push(Forum.events.reopened(at, by))
      );
      default -> throw new IllegalStateException("Unexpected values: " + this);
    };
  }
}
