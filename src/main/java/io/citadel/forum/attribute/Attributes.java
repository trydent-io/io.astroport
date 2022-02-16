package io.citadel.forum.attribute;

import java.util.Optional;
import java.util.UUID;

public enum Attributes {
  Defaults;

  public Optional<ForumID> forumID(String value) {
    try {
      return Optional.of(new ForumID(UUID.fromString(value)));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public Optional<ForumID> forumID() { return Optional.of(new ForumID(UUID.randomUUID())); }
}
