package io.citadel.forum.attribute;

import io.citadel.kernel.domain.Domain;

import java.util.UUID;

public record ForumID(UUID get) implements Domain.ID<UUID> {
}
