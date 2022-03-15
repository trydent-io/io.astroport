package io.citadel.context.forum.state;

import io.citadel.context.forum.Forum;
import io.citadel.context.forum.model.Model;
import io.citadel.context.member.Member;
import io.citadel.shared.context.Domain;

import java.util.stream.Stream;

import static io.citadel.context.forum.Forum.State.Initial;
import static io.citadel.context.forum.Forum.State.Registered;

public sealed interface Registerable extends Domain.Aggregate<Forum, Forum.Model, Forum.State> permits Forum {
  default Forum register(Forum.Name name, Forum.Description description, Member.ID by) {
    return new States.Aggregate(
      this.nextIf(
        Initial,
        Registered,
        model -> new Forum.Model(
          name,
          description
        )
      )
    );
  }
}
