package io.citadel.domain.forum;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.forum.handler.Events;

import java.util.Optional;
import java.util.UUID;

public enum Defaults {
  Companion;

  Forum.Model snapshot(Forum.Model model, Forum.Event event) {
    return switch (event) {
      case Events.Registered registered -> new Forum.Model(model.id(), registered.details());
      case Events.Closed closed -> model;
      case Events.Archived archived -> model;
      case Events.Replaced replaced -> new Forum.Model(model.id(), replaced.details());
      case Events.Reopened reopened -> model;
      case Events.Opened opened -> model;
    };
  }

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
