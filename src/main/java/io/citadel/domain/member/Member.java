package io.citadel.domain.member;

import io.citadel.domain.member.state.States;
import io.citadel.domain.member.state.States.Initial;
import io.citadel.shared.domain.Domain;

import java.util.UUID;

public sealed interface Member permits Initial {
  States states = States.Defaults;

  interface Command extends Domain.Command {}
  interface Event extends Domain.Event {}

  record ID(UUID value) implements Domain.ID<UUID> {}
}
