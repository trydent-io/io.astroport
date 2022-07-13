package io.citadel.kernel.eventstore.metadata;

import io.citadel.kernel.eventstore.audit.Event;
import io.citadel.kernel.eventstore.audit.ID;
import io.citadel.kernel.eventstore.audit.Timepoint;

public record Metadata(ID id, MetaAggregate aggregate, Event event, Timepoint timepoint) {}
