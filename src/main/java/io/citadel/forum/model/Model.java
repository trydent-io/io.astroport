package io.citadel.forum.model;

import io.citadel.forum.Forum;
import io.citadel.member.MemberID;

import java.time.LocalDateTime;

public record Model(
  Forum.ID id,
  Forum.Name name,
  Forum.Description description,
  LocalDateTime openedAt,
  LocalDateTime closedAt,
  MemberID openedBy,
  MemberID closedBy
) {
  public static Model with(Forum.ID identify) { return new Model(identify, null, null, null, null, null, null); }
  public Model name(Forum.Name name) {
    return new Model(id, name, description, openedAt, closedAt, openedBy, closedBy);
  }

  public Model description(Forum.Description description) {
    return new Model(id, name, description, openedAt, closedAt, openedBy, closedBy);
  }

  public Model openedAt(LocalDateTime openedAt) {
    return new Model(id, name, description, openedAt, closedAt, openedBy, closedBy);
  }

  public Model closedAt(LocalDateTime closedAt) {
    return new Model(id, name, description, openedAt, closedAt, openedBy, closedBy);
  }

  public Model openedBy(MemberID openedBy) {
    return new Model(id, name, description, openedAt, closedAt, openedBy, closedBy);
  }

  public Model closedBy(MemberID closedBy) {
    return new Model(id, name, description, openedAt, closedAt, openedBy, closedBy);
  }
}
