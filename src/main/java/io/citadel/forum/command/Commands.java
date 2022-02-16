package io.citadel.forum.command;

import io.citadel.member.MemberID;

import java.time.LocalDateTime;
import java.util.UUID;

public enum Commands {
  Defaults;

  public Open open(String name, final String description, final MemberID by, final LocalDateTime at) { return new Open(name, description, by, at); }
  public Close close
}
