package io.citadel.forum.event;

import io.citadel.forum.Forum;
import io.citadel.forum.command.Open;
import io.citadel.member.MemberID;

import java.time.LocalDateTime;

public record Opened(LocalDateTime at, MemberID by) implements Forum.Event {
  @Override
  public Forum.Command asCommand() {
    return new Open(title, description, by, at);
  }
}
