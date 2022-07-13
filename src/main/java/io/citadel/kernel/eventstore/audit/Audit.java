package io.citadel.kernel.eventstore.audit;

import java.util.UUID;

import static io.citadel.kernel.eventstore.audit.EntityModel.*;

public record Audit(UUID id, Entity entity, Event event, Timepoint timepoint) {
  public static final EntityModel Entity = Companion;
}
