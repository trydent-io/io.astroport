package io.citadel.forum.command;

import io.citadel.forum.Forum;
import io.citadel.member.MemberID;

import java.time.LocalDateTime;

public enum Commands {
  Defaults;

  public Open open(LocalDateTime at, MemberID by) { return new Open(at, by); }

  public Close close(LocalDateTime at, MemberID by) { return new Close(at, by); }

  public Register register(String name, String description, LocalDateTime at, MemberID by) { return new Register(name, description, at, by); }

  public Edit edit(Forum.Name name) { return new Edit.Name(name); }

  public Edit edit(Forum.Description description) { return new Edit.Description(description); }

  public Reopen reopen(LocalDateTime at, MemberID memberID) { return new Reopen(at, memberID); }
}
