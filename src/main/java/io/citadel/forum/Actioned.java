package io.citadel.forum;

import io.citadel.member.MemberID;

import java.time.LocalDateTime;

public record Actioned(LocalDateTime at, MemberID by) {}
