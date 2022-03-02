package io.citadel.domain.forum.model;

import io.citadel.domain.forum.Actioned;
import io.citadel.domain.forum.Forum;

public record Model(Forum.Name name, Forum.Description description, Actioned registered, Actioned opened, Actioned closed, Actioned reopened) {
  public Model() { this(null, null, null, null, null, null); }

  public Model name(Forum.Name name) {
    return new Model(name, description, registered, opened, closed, reopened);
  }

  public Model description(Forum.Description description) {
    return new Model(name, description, registered, opened, closed, reopened);
  }

  public Model registered(Actioned registered) {
    return new Model(name, description, registered, opened, closed, reopened);
  }

  public Model opened(Actioned opened) {
    return new Model(name, description, registered, opened, closed, reopened);
  }

  public Model closed(Actioned closed) {
    return new Model(name, description, registered, opened, closed, reopened);
  }

  public Model reopened(final Actioned reopened) {
    return new Model(name, description, registered, opened, closed, reopened);
  }
}
