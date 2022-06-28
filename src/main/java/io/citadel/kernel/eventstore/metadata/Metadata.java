package io.citadel.kernel.eventstore.metadata;

public record Metadata(ID id, Aggregate aggregate, Event event, Timepoint timepoint) {}
