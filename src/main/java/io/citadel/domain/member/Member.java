package io.citadel.domain.member;

import io.citadel.kernel.domain.Domain;
import io.vertx.core.Future;

import java.time.LocalDate;
import java.util.Optional;

public sealed interface Member extends Domain.Aggregate<Member.ID, Member.Model, Member.Event, Member> {
  static Member boundary(io.citadel.kernel.eventstore.metadata.Aggregate<Model, Event> aggregate) {
    return new Aggregate(aggregate);
  }

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

  record Model(Member.ID id, FirstName firstName, LastName lastName, Birthdate birthdate, FiscalCode fiscalCode) {}
}

final class Aggregate implements Member {
  private final io.citadel.kernel.eventstore.metadata.Aggregate<Model, State, Event> aggregate;

  Aggregate(io.citadel.kernel.eventstore.metadata.Aggregate<Model, Event> aggregate) {
    this.aggregate = aggregate;
  }

  @Override
  public Future<io.citadel.kernel.eventstore.metadata.Aggregate<Model, Event>> load(ID id, long version) {
    return null;
  }
}
