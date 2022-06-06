package io.citadel.domain.member;

import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.domain.attribute.Attribute;
import io.citadel.kernel.eventstore.Context;
import io.citadel.kernel.lang.Snowflake;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.time.LocalDate;
import java.util.UUID;

public sealed interface Member extends Domain.Aggregate<Member.ID, Member.Model, Member.Event, Member> {
  static Member boundary(Context<Model, Event> context) {
    return new Root(context);
  }

  interface Command extends Domain.Command {}
  interface Event extends Domain.Event {}

  record ID(String value) implements Domain.ID<String> {
    public static ID uuid() { return new ID(UUID.randomUUID().toString()); }
    public static ID random() { return new ID(Snowflake.Default.nextAsString()); }
    public static ID from(JsonObject json) { return new ID(json.getString("id")); }
  }

  record FirstName(String value) implements Attribute<String> {}
  record LastName(String value) implements Attribute<String> {}
  record Birthdate(LocalDate value) implements Attribute<LocalDate> {}
  record FiscalCode(String value) implements Attribute<String> {}

  record Model(Member.ID id, FirstName firstName, LastName lastName, Birthdate birthdate, FiscalCode fiscalCode) implements Domain.Model<Member.ID> {}
}

final class Root implements Member {
  private final Context<Member.Model, Member.Event> context;

  Root(Context<Model, Event> context) {
    this.context = context;
  }

  @Override
  public Future<Context<Model, Event>> load(ID id, long version) {
    return null;
  }
}
