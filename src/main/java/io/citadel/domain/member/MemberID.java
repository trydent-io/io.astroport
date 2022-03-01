package io.citadel.domain.member;

import io.citadel.shared.domain.Domain;

import java.util.UUID;

public record MemberID(UUID get) implements Domain.ID<UUID> {}
