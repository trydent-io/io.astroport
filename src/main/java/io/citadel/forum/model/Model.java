package io.citadel.forum.model;

import io.citadel.forum.Forum;
import io.citadel.member.MemberID;

import java.time.LocalDateTime;

public record Model(
  Forum.ID forumID,
  Forum.Name name,
  Forum.Description description,
  LocalDateTime openedAt,
  LocalDateTime closedAt,
  MemberID openedBy,
  MemberID closedBy
) {
  public static Model with(Forum.ID identify) { return new Model(identify, null, null, null, null, null, null); }
  public Model name(String name) {
    return new Model(forumID, Forum.attributes.name(name).orElseThrow(), description, openedAt, closedAt, openedBy, closedBy);
  }

  public Model description(String description) {
    return new Model(forumID, name, Forum.attributes.description(description).orElseThrow(), openedAt, closedAt, openedBy, closedBy);
  }

  public Model openedAt(LocalDateTime openedAt) {
    return new Model(forumID, name, description, openedAt, closedAt, openedBy, closedBy);
  }

  public Model closedAt(LocalDateTime closedAt) {
    return new Model(forumID, name, description, openedAt, closedAt, openedBy, closedBy);
  }

  public Model openedBy(MemberID openedBy) {
    return new Model(forumID, name, description, openedAt, closedAt, openedBy, closedBy);
  }

  public Model closedBy(MemberID closedBy) {
    return new Model(forumID, name, description, openedAt, closedAt, openedBy, closedBy);
  }
}
