package io.citadel.forum.event;

import io.citadel.forum.Forum;
import io.citadel.forum.command.Register;
import io.citadel.member.MemberID;

import java.time.LocalDateTime;

public record Registered(Forum.Name name, Forum.Description description, LocalDateTime at, MemberID by) implements Forum.Event {}
