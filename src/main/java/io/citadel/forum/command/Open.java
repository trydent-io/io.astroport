package io.citadel.forum.command;

import io.citadel.forum.Forum;
import io.citadel.forum.event.Opened;
import io.citadel.kernel.domain.Domain;
import io.citadel.member.MemberID;

import java.time.LocalDateTime;

public record Open(String title, String description, MemberID by, LocalDateTime at) implements Forum.Command {
  @Override
  public Domain.Event[] asEvents() {
    return new Domain.Event[]{
      new Opened(title, description, by, at)
    };
  }

  @Override
  public Forum apply(Forum forum) {
    return null;
  }
}
