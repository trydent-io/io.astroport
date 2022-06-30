package io.citadel.kernel.eventstore.metadata;

public record Metadata(ID id, MetaAggregate aggregate, Event event, Timepoint timepoint) {}
