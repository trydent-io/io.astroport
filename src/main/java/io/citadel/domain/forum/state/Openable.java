package io.citadel.domain.forum.state;

import io.citadel.domain.forum.Actioned;
import io.citadel.domain.forum.Forum;
import io.citadel.shared.domain.Domain;
import io.citadel.domain.member.Member;

import java.time.LocalDateTime;

public sealed interface Openable extends Domain.Aggregate<Forum> permits Forum {
  default Forum open(LocalDateTime at, Member.ID by) {
    return switch (this) {
      case States.Registered it -> new States.Open(
        it.id(),
        it.version(),
        it.model().opened(new Actioned(at, by)),
        it.events().push(Forum.events.opened(at, by))
      );
      default -> throw new IllegalStateException("Unexpected value: " + this);
    };
  }

  default Forum reopen(LocalDateTime at, Member.ID by) {
    return switch (this) {
      case States.Closed it -> new States.Open(
        it.id(),
        it.version(),
        it.model().reopened(new Actioned(at, by)),
        it.events().push(Forum.events.reopened(at, by))
      );
      default -> throw new IllegalStateException("Unexpected values: " + this);
    };
  }
}
