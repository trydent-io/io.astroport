package io.citadel.domain.forum.handler;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.forum.aggregate.Forums;
import io.citadel.domain.member.Member;

import java.time.LocalDateTime;

public enum Commands {
  Companion;

  public Forum.Command open(LocalDateTime at) { return new Open(at); }

  public Forum.Command close(LocalDateTime at) { return new Close(at); }

  public Forum.Command register(Forum.Name name, Forum.Description description, LocalDateTime at) { return new Register(name, description, at); }

  public Forum.Command alter(Forum.Name name, Forum.Description description) { return new Alter(name, description); }

  public Forum.Command reopen(LocalDateTime at, Member.ID memberID) { return new Reopen(at, memberID); }

  public record Close(LocalDateTime at) implements Forum.Command {}

  public record Open(LocalDateTime at) implements Forum.Command {}

  public record Register(Forum.Name name, Forum.Description description, LocalDateTime at) implements Forum.Command {}

  public record Reopen(LocalDateTime at, Member.ID memberID) implements Forum.Command {}

  public record Alter(Forum.Name name, Forum.Description description) implements Forum.Command {}

  public record Archive() implements Forum.Command {}
}
