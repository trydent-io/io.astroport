package io.citadel.context.member.state;

import io.citadel.context.member.Member;
import io.citadel.shared.context.Domain;

import java.util.stream.Stream;

public enum States {
  Defaults;

  public Member of(Member.ID identity) {
    return new Initial(identity, Domain.Version.first());
  }

  public Member from(Member.ID identity, Domain.Version version, Member.Event... events) {
    return Stream.of(events).reduce(Member.states.of(identity, version), (member, event) -> member, (f, f2) -> f2);
  }

  public Member of(Member.ID identity, Domain.Version version) {
    return new Initial(identity, version);
  }

  public record Initial(ID id, Domain.Version version) implements Member {}
}
