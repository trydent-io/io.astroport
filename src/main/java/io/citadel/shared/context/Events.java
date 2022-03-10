package io.citadel.shared.context;

import io.vertx.core.json.JsonObject;

import java.time.LocalDateTime;
import java.util.concurrent.Future;

public interface Events {

  record Stored(String eventName, JsonObject eventData, LocalDateTime persistedAt) {}

  Future<Domain.Event[]> findBy(Domain.ID<?> id, Domain.Version version, String name);
  default Future<Domain.Event[]> findBy(Domain.ID<?> id, String name) {
    return findBy(id, Domain.Version.last(), name);
  }

}
