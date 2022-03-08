package io.citadel.context.forum.state;

import io.citadel.context.forum.Forum;
import io.citadel.context.forum.model.Model;
import io.citadel.context.member.Member;
import io.citadel.shared.domain.Domain;
import io.citadel.shared.lang.Array;

import static io.citadel.context.forum.Forum.State.Initial;

public sealed interface Registerable extends Domain.Aggregate<Forum.State> permits Forum {
  default Forum register(Forum.Name name, Forum.Description description, Member.ID by) {
    return switch (this) {
      case States.Aggregate it && it.is(Initial) -> Forum.states.registered(
        it.id(),
        it.version(),
        new Model()
          .name(name)
          .description(description),
        Array.of(Forum.events.registered(name, description, by))
      );
      default -> throw new IllegalStateException("Unexpected value: " + this);
    };
  }
}
