package io.citadel.domain.forum.state;

import io.citadel.domain.forum.Actioned;
import io.citadel.domain.forum.Forum;
import io.citadel.domain.forum.model.Model;
import io.citadel.shared.domain.Domain;
import io.citadel.shared.media.Array;
import io.citadel.domain.member.Member;

import java.time.LocalDateTime;

public sealed interface Registerable extends Domain.Aggregate<Forum> permits Forum {
  default Forum register(Forum.Name name, Forum.Description description, LocalDateTime at, Member.ID by) {
    return switch (this) {
      case States.Initial it -> new States.Registered(
        it.id(),
        it.version(),
        new Model()
          .name(name)
          .description(description)
          .registered(new Actioned(at, by)),
        Array.of(Forum.events.registered(name, description, at, by))
      );
      default -> throw new IllegalStateException("Unexpected value: " + this);
    };
  }
}
