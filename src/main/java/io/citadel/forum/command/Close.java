package io.citadel.forum.command;

import io.citadel.forum.Forum;
import io.citadel.forum.event.Closed;
import io.citadel.forum.event.Opened;
import io.citadel.kernel.domain.Domain;
import io.citadel.member.MemberID;

import java.time.LocalDateTime;

public record Close(LocalDateTime at, MemberID by) implements Forum.Command {
  @Override
  public Domain.Event asEvent() {
    return new Closed(at, by);
  }
}
