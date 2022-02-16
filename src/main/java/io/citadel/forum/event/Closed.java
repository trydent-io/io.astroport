package io.citadel.forum.event;

import io.citadel.kernel.domain.Domain;
import io.citadel.member.MemberID;

import java.time.LocalDateTime;

public record Closed(LocalDateTime at, MemberID by) implements Domain.Event {
  @Override
  public Domain.Command asCommand() {
    return null;
  }
}
