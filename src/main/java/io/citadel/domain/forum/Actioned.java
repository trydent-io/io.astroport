package io.citadel.domain.forum;

import io.citadel.domain.member.Member;

import java.time.LocalDateTime;

public record Actioned(LocalDateTime at, Member.ID by) {}
