package io.citadel.domain.member;

import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.lang.Snowflake;
import io.vertx.core.json.JsonObject;

import java.util.UUID;

public sealed interface Member {
  enum Namespace implements Member {}

  interface Command extends Domain.Command {}
  interface Event extends Domain.Event {}

  record ID(String value) implements Domain.ID {
    public static ID uuid() { return new ID(UUID.randomUUID().toString()); }
    public static ID random() { return new ID(Snowflake.Default.nextAsString()); }
    public static ID from(JsonObject json) { return new ID(json.getString("id")); }
  }
}
