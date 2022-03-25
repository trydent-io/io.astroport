package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;

import java.time.LocalDateTime;

record Model(Forum.Name name, Forum.Description description, LocalDateTime openAt) {
  Model() { this(null, null, null); }

  public Model name(final Forum.Name name) {
    return new Model(name, description, openAt);
  }

  public Model description(final Forum.Description description) {
    return new Model(name, description, openAt);
  }

  public Model openAt(final LocalDateTime openAt) {
    return new Model(name, description, this.openAt);
  }
}

public final class Aggregate {
  private final Forum.ID id;
  private final long version;
  private final Model model;

  public Aggregate(final Forum.ID id, final long version, final Model model) {
    this.id = id;
    this.version = version;
    this.model = model;
  }

  public Aggregate register(Forum.Name name, Forum.Description description) {
    return new Aggregate(
      id,
      version,
      new Model()
        .name(name)
        .description(description)
    );
  }

  public Aggregate open(LocalDateTime at) {
    return new Aggregate(id, version, model.openAt(at));
  }
}

final class Staging {
  private final Aggregate aggregate;
  private final Forum.State state;

  Staging(final Aggregate aggregate, final Forum.State state) {
    this.aggregate = aggregate;
    this.state = state;
  }
}
