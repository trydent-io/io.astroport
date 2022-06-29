package io.citadel.domain.member;

import java.time.LocalDate;
import java.util.Optional;

public sealed interface Member {
  enum State implements io.citadel.kernel.domain.State<State, Event> {
    Registered, Onboarded, Offboarded, Unregistered;

    @Override
    public Optional<State> transit(Event event) {
      return Optional.empty();
    }
  }

  interface Command {}
  interface Event {}

  record ID(String value) {
  }

  record FirstName(String value) {}
  record LastName(String value) {}
  record Birthdate(LocalDate value) {}
  record FiscalCode(String value) {}

  record Entity(Member.ID id, FirstName firstName, LastName lastName, Birthdate birthdate, FiscalCode fiscalCode) {}
}

