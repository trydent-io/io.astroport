package io.citadel.domain.forum.model;

import io.citadel.domain.forum.Forum;
import io.citadel.kernel.domain.Domain;

import java.util.Optional;
import java.util.UUID;

public enum Attributes {
  Companion;

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

  public Forum.ID id(final String it) {
    return new Forum.ID(UUID.fromString(it));
  }
}
