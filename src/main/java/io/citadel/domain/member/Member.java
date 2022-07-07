package io.citadel.domain.member;

import java.time.LocalDate;

public sealed interface Member {
  enum State implements io.citadel.kernel.domain.State<State, Event> {
    Registered, Onboarded, Offboarded, Unregistered;

    @Override
    public State transit(Event event) {
      return State.Registered;
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

  record Entity(FirstName firstName, LastName lastName, Birthdate birthdate, FiscalCode fiscalCode) {}
}

final class Aggregate implements Member {
  private final Member.ID id;
  private final Member.Entity entity;
  private final Transaction transaction;
}
