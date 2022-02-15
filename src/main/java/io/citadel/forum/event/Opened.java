package io.citadel.forum.event;

import io.citadel.domain.message.Command;
import io.citadel.domain.message.Event;
import io.citadel.forum.Forum;
import io.citadel.forum.command.Commands;
import io.citadel.forum.command.Open;
import io.vertx.core.json.JsonObject;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public record Opened(String title, String description, UUID by, LocalDateTime at) implements Event {
  static Optional<Event> from(JsonObject json) {
    return Optional.ofNullable(json).map(it -> it.mapTo(Opened.class));
  }

  @Override
  public Command asCommand() {
    return Forum.commands.open(title, description, by, at);
  }
}
