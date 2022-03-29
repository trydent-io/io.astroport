package io.citadel.domain.forum.state;

import io.citadel.domain.forum.Forum;

public enum States {
  Defaults;

  public Forum of(Forum.ID identity) {
    return identity(identity, 0);
  }

  public Forum identity(Forum.ID identity, long version) {
    return null;
  }

}
