package io.citadel.forum.command;

import io.citadel.forum.Forum;
import io.citadel.forum.event.Registered;
import io.citadel.member.MemberID;

import java.time.LocalDateTime;

public record Register(String name, String description, LocalDateTime at, MemberID by) implements Forum.Command {
  @Override
  public Forum.Event asEvent() {
    return new Registered(name, description, at, by);
  }
}
