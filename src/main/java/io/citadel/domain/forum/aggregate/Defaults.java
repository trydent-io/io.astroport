package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;

public enum Defaults {
  Defaults;

  public Forum.Aggregate snapshot(Forum.ID identity) {
    return new Snapshot(new Forum.Model(identity, null));
  }
}
