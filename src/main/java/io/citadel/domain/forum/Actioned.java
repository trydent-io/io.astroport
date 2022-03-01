package io.citadel.domain.forum;

import io.citadel.domain.member.MemberID;

import java.time.LocalDateTime;

public record Actioned(LocalDateTime at, MemberID by) {}
