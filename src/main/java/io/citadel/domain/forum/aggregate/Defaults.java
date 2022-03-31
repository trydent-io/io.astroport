package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;

public enum Defaults {
  Companion;

  public Service<Model> with(Forum.ID id) {
    return Service.lifecycle(new Model(id));
  }
}
