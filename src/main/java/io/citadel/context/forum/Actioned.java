package io.citadel.context.forum;

import io.citadel.context.member.Member;

import java.time.LocalDateTime;

public record Actioned(LocalDateTime at, Member.ID by) {}
