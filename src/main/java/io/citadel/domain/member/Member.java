package io.citadel.domain.member;

import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.eventstore.Context;
import io.vertx.core.Future;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Consumer;

public sealed interface Member extends Domain.Aggregate<Member.ID, Member.Model, Member.Event, Member> {
  static Member boundary(Context<Model, Event> context) {
    return new Aggregate(context);
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
  private final Context<Member.Model, Member.State, Member.Event> context;

  Aggregate(Context<Model, Event> context) {
    this.context = context;
  }

  @Override
  public Future<Context<Model, Event>> load(ID id, long version) {
    return null;
  }
}
