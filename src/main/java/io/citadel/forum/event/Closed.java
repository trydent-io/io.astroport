package io.citadel.forum.event;

import io.citadel.domain.message.Command;
import io.citadel.domain.message.Event;
import io.vertx.core.json.JsonObject;

import java.util.Optional;

public record Closed() implements Event {
  public static Optional<Event> from(JsonObject json) {
    return Optional.ofNullable(json).map(it -> new Closed());
  }

  @Override
  public Command asCommand() {
    return null;
  }
}
