package io.citadel.domain.forum.command;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.member.Member;

import java.time.LocalDateTime;

public enum Commands {
  Companion;

  public Forum.Command open(LocalDateTime at, Member.ID by) { return new Open(at, by); }

  public Forum.Command close(LocalDateTime at, Member.ID by) { return new Close(at, by); }

  public Forum.Command register(String name, String description, LocalDateTime at, Member.ID by) { return new Register(name, description, at, by); }

  public Forum.Command change(Forum.Name name, Forum.Description description) { return new Change(name, description); }

  public Forum.Command reopen(LocalDateTime at, Member.ID memberID) { return new Reopen(at, memberID); }

  public record Close(LocalDateTime at, Member.ID by) implements Forum.Command {}

  public record Open(LocalDateTime at, Member.ID by) implements Forum.Command {}

  public record Register(String name, String description, LocalDateTime at, Member.ID by) implements Forum.Command {}

  public record Reopen(LocalDateTime at, Member.ID memberID) implements Forum.Command {}

  public record Change(Forum.Name name, Forum.Description description) implements Forum.Command {}
}
