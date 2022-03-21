package io.citadel.domain.forum.state;

import io.citadel.domain.forum.Forum;
import io.citadel.kernel.domain.Domain;

public enum States {
  Defaults;

  public Forum of(Forum.ID identity) {
    return identity(identity, 0);
  }

  public Forum identity(Forum.ID identity, long version) {
    return new Aggregate(Domain.Model.identity(identity, version, Forum.State.Initial));
  }

  public record Aggregate(Domain.Model.Identity<Forum.Model> identity) implements Forum {}
}
