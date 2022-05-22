package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.kernel.domain.Domain;

public enum Defaults {
  Companion;

  public Forum.Model model(String id) {
    return new Forum.Model(Forum.attributes.id(id));
  }

  public Snapshot snapshot() {
    return new Snapshot(new Stage());
  }

  public Forum.Aggregate aggregate(Forum.Model model, long version, Stage stage, final Domain.Transaction transaction) {
    return new Root(model, version, stage, transaction);
  }
}
