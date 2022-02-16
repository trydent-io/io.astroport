package io.citadel.member;

import io.citadel.kernel.domain.Domain;

import java.util.UUID;

public record MemberID(UUID get) implements Domain.ID<UUID> {}
