package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;

public enum Defaults {
  Companion;

  public Service<Model> model(Forum.ID id) {
    return Service.lifecycle(new Model(id));
  }

  public Aggregate aggregate(Forum.ID id, long version) {
    return Aggregate.transaction(Service.lifecycle(Aggregate.root(id, version)));
  }

  public Aggregate aggregate(Model model, long version) {
    return Aggregate.transaction(Service.lifecycle(Aggregate.root(model, version)));
  }
}
