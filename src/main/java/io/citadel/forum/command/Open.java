package io.citadel.forum.command;

import io.citadel.domain.message.Command;
import io.citadel.domain.message.Event;
import io.citadel.forum.event.Opened;

import java.time.LocalDateTime;
import java.util.UUID;

public record Open(String title, String description, UUID by, LocalDateTime at) implements Command {
  @Override
  public Event[] asEvents() {
    return new Event[] {
      new Opened(title, description, by, at)
    };
  }
}
