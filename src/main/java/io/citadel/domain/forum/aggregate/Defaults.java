package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;

public enum Defaults {
  Companion;

  public Snap snapshot(Forum.ID id) {
    return Snap.shot(id);
  }
}
