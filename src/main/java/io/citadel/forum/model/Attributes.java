package io.citadel.forum.model;

import io.citadel.forum.Forum;

import java.util.Optional;
import java.util.UUID;

public enum Attributes {
  Defaults;

  public Optional<Forum.ID> ID(String value) {
    try {
      return Optional.of(new Forum.ID(UUID.fromString(value)));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public Forum.ID ID() { return new Forum.ID(UUID.randomUUID()); }
}
