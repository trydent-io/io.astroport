package io.citadel.forum.model;

import io.citadel.forum.Actioned;
import io.citadel.forum.Forum;
import io.citadel.member.MemberID;

import java.time.LocalDateTime;

public record State(
  Forum.Name name,
  Forum.Description description,
  Actioned registered,
  Actioned opened,
  Actioned closed
) {
  public State() { this(null, null, null, null, null); }

  public State name(Forum.Name name) {
    return new State(name, description, registered, opened, closed);
  }

  public State description(Forum.Description description) {
    return new State(name, description, registered, opened, closed);
  }

  public State registered(Actioned registered) {
    return new State(name, description, registered, opened, closed);
  }

  public State opened(Actioned opened) {
    return new State(name, description, registered, opened, closed);
  }

  public State closed(Actioned closed) {
    return new State(name, description, registered, opened, closed);
  }
}
