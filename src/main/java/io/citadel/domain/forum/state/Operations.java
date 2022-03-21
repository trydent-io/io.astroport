package io.citadel.domain.forum.state;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.member.Member;
import io.citadel.kernel.domain.Domain;

import static io.citadel.domain.forum.Forum.State.Closed;
import static io.citadel.domain.forum.Forum.State.Initial;
import static io.citadel.domain.forum.Forum.State.Open;
import static io.citadel.domain.forum.Forum.State.Registered;
import static io.citadel.domain.forum.state.States.*;

public sealed interface Operations extends Domain.Aggregate permits Forum {
  default Forum register(Forum.Name name, Forum.Description description, Member.ID by) {
    return switch (this) {
      case Aggregate aggregate -> aggregate.model().nextIf(
        Initial,
        Registered,
        model -> new Forum.Model(
          name,
          description
        )
      );
    };
  }

  default Forum open(Member.ID by) {
    return nextIf(
      Registered,
      Open,
      model -> model
    );
  }

  default Forum reopen(Member.ID by) {
    return nextIf(
      Closed,
      Open,
      model -> model
    );
  }

  default Forum edit(Forum.Name name) {
    return stayIf(
      Registered.or(Open),
      model -> new Forum.Model(name, model.description())
    );
  }

  default Forum edit(Forum.Description description) {
    return stayIf(
      Registered.or(Open),
      model -> new Forum.Model(model.name(), description)
    );
  }

  default Forum close(Member.ID by) {
    return nextIf(
      Open,
      Closed,
      model -> model
    );
  }
}
