package io.citadel.domain.forum.command;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.member.Member;

import java.time.LocalDateTime;

public enum Commands {
  Defaults;

  public Open open(LocalDateTime at, Member.ID by) { return new Open(at, by); }

  public Close close(LocalDateTime at, Member.ID by) { return new Close(at, by); }

  public Register register(String name, String description, LocalDateTime at, Member.ID by) { return new Register(name, description, at, by); }

  public Edit edit(Forum.Name name) { return new Edit.Name(name); }

  public Edit edit(Forum.Description description) { return new Edit.Description(description); }

  public Reopen reopen(LocalDateTime at, Member.ID memberID) { return new Reopen(at, memberID); }

  public record Close(LocalDateTime at, Member.ID by) implements Forum.Command {}

  public record Open(LocalDateTime at, Member.ID by) implements Forum.Command {}

  public record Register(String name, String description, LocalDateTime at, Member.ID by) implements Forum.Command {}

  public record Reopen(LocalDateTime at, Member.ID memberID) implements Forum.Command {}

  public sealed interface Edit extends Forum.Command {
    record Description(Forum.Description description) implements Edit {}
    record Name(Forum.Name name) implements Edit {}
  }
}
