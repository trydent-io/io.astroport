package io.citadel.forum.event;

import io.citadel.forum.Forum;
import io.citadel.forum.command.Reopen;
import io.citadel.member.MemberID;

import java.time.LocalDateTime;

public record Reopened(LocalDateTime at, MemberID memberID) implements Forum.Event {
  @Override
  public Forum.Command asCommand() {
    return new Reopen(at, memberID);
  }
}
