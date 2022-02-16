package io.citadel.forum.event;

import io.citadel.kernel.domain.Domain;
import io.vertx.core.json.JsonObject;

import java.util.Optional;

public enum Events {
  Defaults;

  private enum Names { Opened, Closed }

  public Optional<Domain.Event> from(String name, JsonObject json) {
    try {
      return switch (Names.valueOf(name)) {
        case Opened -> Opened.from(json);
        case Closed -> Closed.from(json);
      };
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }
}
