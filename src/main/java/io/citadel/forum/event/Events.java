package io.citadel.forum.event;

import io.citadel.forum.Forum;
import io.citadel.kernel.domain.Domain;
import io.vertx.core.json.JsonObject;

import java.util.Optional;

public enum Events {
  Defaults;

  public Forum.Event edited(final Forum.Description description) {
    return new Edited.Description(description);
  }

  public Forum.Event edited(final Forum.Name name) {
    return new Edited.Name(name);
  }

  private enum Names { Opened, Closed }

  public Optional<Domain.Event> from(String name, JsonObject json) {
    try {
      return Optional.of(switch (Names.valueOf(name)) {
        case Opened -> json.mapTo(Opened.class);
        case Closed -> json.mapTo(Closed.class);
      });
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }
}
