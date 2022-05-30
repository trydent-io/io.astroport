package io.citadel.kernel.eventstore;

import io.citadel.kernel.domain.Domain;

record Aggregate<M extends Record & Domain.Model<?>>(M model, String name, long version) {}
