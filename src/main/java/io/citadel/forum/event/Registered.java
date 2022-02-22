package io.citadel.forum.event;

import io.citadel.forum.Forum;
import io.citadel.forum.command.Register;
import io.citadel.member.MemberID;

import java.time.LocalDateTime;

public record Registered(String name, String description, LocalDateTime at, MemberID by) implements Forum.Event {
  @Override
  public Forum.Command asCommand() {
    return new Register(name, description, at, by);
  }
}
