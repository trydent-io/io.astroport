package io.citadel.forum.event;

import io.citadel.kernel.domain.Domain;
import io.citadel.forum.Forum;
import io.vertx.core.json.JsonObject;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public record Opened(String title, String description, UUID by, LocalDateTime at) implements Domain.Event {
  static Optional<Domain.Event> from(JsonObject json) {
    return Optional.ofNullable(json).map(it -> it.mapTo(Opened.class));
  }

  @Override
  public Domain.Command asCommand() {
    return Forum.commands.open(title, description, by, at);
  }
}
