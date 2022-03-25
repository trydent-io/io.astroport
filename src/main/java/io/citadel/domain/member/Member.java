package io.citadel.domain.member;

import io.citadel.kernel.domain.Domain;
import io.vertx.core.json.JsonObject;

import java.util.UUID;

public sealed interface Member {
  enum Namespace implements Member {}

  interface Command extends Domain.Command {}
  interface Event extends Domain.Event {}

  record ID(UUID value) implements Domain.ID<UUID> {
    public static ID from(JsonObject json) { return new ID(UUID.fromString(json.getString("id"))); }
  }
}
