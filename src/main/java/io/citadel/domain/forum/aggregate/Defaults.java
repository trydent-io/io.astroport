package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;

public enum Defaults {
  Defaults;

  public Forum.Snapshot snapshot(Forum.ID identity) {
    return new Sourcing(new Forum.Model(identity, null));
  }
}
