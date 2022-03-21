package io.citadel.domain.member;

import io.citadel.kernel.domain.Domain;

import java.util.UUID;

public sealed interface Member {
  enum Namespace implements Member {}

  interface Command extends Domain.Command {}
  interface Event extends Domain.Event {}

  record ID(UUID value) implements Domain.ID<UUID> {}
}
