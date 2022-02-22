package io.citadel.forum.command;

import io.citadel.forum.Forum;
import io.citadel.forum.event.Reopened;
import io.citadel.member.MemberID;

import java.time.LocalDateTime;

public record Reopen(LocalDateTime at, MemberID memberID) implements Forum.Command {
  @Override
  public Forum.Event asEvent() {
    return new Reopened(at, memberID);
  }
}
