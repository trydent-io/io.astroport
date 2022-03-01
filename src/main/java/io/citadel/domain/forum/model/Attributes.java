package io.citadel.domain.forum.model;

import io.citadel.domain.forum.Forum;

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

  public Optional<Forum.Name> name(String value) {
    return Optional.ofNullable(value)
      .filter(it -> it.length() > 3 && it.length() <= 255)
      .map(Forum.Name::new);
  }

  public Optional<Forum.Description> description(String value) {
    return Optional.ofNullable(value)
      .filter(it -> it.length() > 3 && it.length() <= 4000)
      .map(Forum.Description::new);
  }
}
