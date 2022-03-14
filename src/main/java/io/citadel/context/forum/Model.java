package io.citadel.context.forum;

import io.citadel.context.member.Member;
import io.citadel.shared.context.Domain;

public record Model(Forum.Name name, Forum.Description description, Member.ID registeredBy, Member.ID openedBy, Member.ID closedBy, Member.ID reopenedBy) implements Domain.Model {
  public Model() {this(null, null, null, null, null, null);}

  public Model name(Forum.Name name) {
    return new Model(name, description, registeredBy, openedBy, closedBy, reopenedBy);
  }

  public Model description(Forum.Description description) {
    return new Model(name, description, registeredBy, openedBy, closedBy, reopenedBy);
  }

  public Model registered(Member.ID registeredBy) {
    return new Model(name, description, registeredBy, openedBy, closedBy, reopenedBy);
  }

  public Model opened(Member.ID openedBy) {
    return new Model(name, description, registeredBy, openedBy, closedBy, reopenedBy);
  }

  public Model closed(Member.ID closedBy) {
    return new Model(name, description, registeredBy, openedBy, closedBy, reopenedBy);
  }

  public Model reopened(final Member.ID reopenedBy) {
    return new Model(name, description, registeredBy, openedBy, closedBy, reopenedBy);
  }
}
