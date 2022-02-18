package io.citadel.forum.event;

import io.citadel.kernel.domain.Domain;
import io.citadel.forum.Forum;
import io.citadel.member.MemberID;
import io.vertx.core.json.JsonObject;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public record Opened(String title, String description, MemberID by, LocalDateTime at) implements Forum.Event {
  @Override
  public Domain.Command asCommand() {
    return Forum.commands.open(title, description, by, at);
  }
}
