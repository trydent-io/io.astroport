package io.citadel.forum.state;

import io.citadel.forum.Forum;
import io.citadel.kernel.domain.Domain;

public sealed interface Registerable extends Domain.Aggregate<Forum> permits Forum {
  default Forum register(Forum.Name name, Forum.Description description) {
    return new State(this, );
  }
}
