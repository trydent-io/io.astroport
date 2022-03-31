package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.member.Member;

public record Model(Forum.ID id, Forum.Details details, Member.ID registeredBy, Member.ID openedBy, Member.ID closedBy, Member.ID reopenedBy) implements Forum<Model> {
  public Model(Forum.ID id) {this(id, null, null, null, null, null);}
  public Model(Forum.ID id, Forum.Details details) {this(id, details, null, null, null, null);}

  @Override
  public Model register(final Name name, final Description description) {
    return new Model(id, new Details(name, description));
  }

  @Override
  public Model edit(final Name name, final Description description) {
    return new Model(id, new Details(name, description));
  }

  @Override
  public Model open() {
    return this;
  }

  @Override
  public Model close() {
    return this;
  }

  @Override
  public Model archive() {
    return this;
  }

  @Override
  public Model reopen() {
    return this;
  }
}
