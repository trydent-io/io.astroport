package io.citadel.domain.forum;

import io.citadel.domain.forum.aggregate.Defaults;
import io.citadel.domain.forum.aggregate.Providing;
import io.citadel.domain.forum.aggregate.Snapshot;
import io.citadel.domain.forum.aggregate.Lifecycle;
import io.citadel.domain.forum.command.Commands;
import io.citadel.domain.forum.event.Events;
import io.citadel.domain.forum.model.Attributes;
import io.citadel.domain.forum.repository.History;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.domain.attribute.Attribute;

import java.util.UUID;
import java.util.function.Function;

public sealed interface Forum<F extends Forum<?>> permits Forum.Aggregate, Forum.Life, Forum.Service {
  String AGGREGATE_NAME = "FORUM";

  Commands commands = Commands.Defaults;
  Events event = Events.Defaults;
  Attributes attributes = Attributes.Defaults;
  Defaults defaults = Defaults.Defaults;

  static Forum.Aggregate with(Forum.ID id) {
    return Forum.defaults.snapshot(id);
  }

  enum State implements Domain.State<State> {Registered, Open, Closed, Archived}

  sealed interface Command extends Domain.Command permits Commands.Close, Commands.Edit, Commands.Open, Commands.Register, Commands.Reopen {}

  sealed interface Event extends Domain.Event permits Events.Archived, Events.Closed, Events.Edited, Events.Opened, Events.Registered, Events.Reopened {}

  sealed interface Hydration extends Domain.Hydration<Aggregate> permits History {}

  record ID(UUID value) implements Domain.ID<UUID> {}
  record Name(String value) implements Attribute<String> {}
  record Description(String value) implements Attribute<String> {}
  record Details(Name name, Description description) {}
  record Model(Forum.ID id, Forum.Details details) {}

  sealed interface Life<F extends Forum<F>> extends Forum<Life<F>> {
    record Span<F extends Forum<F>>(State state, F forum) implements Life<F> {
      @Override
      public Life<F> register(Name name, Description description) {
        return switch (state) {
          case null -> new Span<>(State.Registered, forum.register(name, description));
          default -> null;
        };
      }

      @Override
      public Life<F> edit(Name name, Description description) {
        return switch (state) {
          case Registered, Open -> new Span<>(state, forum.edit(name, description));
          default -> null;
        };
      }

      @Override
      public Life<F> open() {
        return switch (state) {
          case Registered -> new Span<>(State.Open, forum.open());
          default -> null;
        };
      }

      @Override
      public Life<F> close() {
        return switch (state) {
          case Open -> new Span<>(State.Closed, forum.close());
          default -> null;
        };
      }

      @Override
      public Life<F> archive() {
        return switch (state) {
          case Closed -> new Span<>(State.Archived, forum.archive());
          default -> null;
        };
      }

      @Override
      public Life<F> reopen() {
        return switch (state) {
          case Closed -> new Span<>(State.Open, forum.reopen());
          default -> null;
        };
      }
    }
  }

  sealed interface Aggregate extends Forum<Aggregate>, Domain.Aggregate<Forum.Service> permits Aggregate.Time, Lifecycle, Snapshot {
    static Aggregate time() {
      return new Time(new Life.Span<>(null, new Snapshot(new Model(null, null))));
    }
    record Time(Life<Aggregate> span) implements Aggregate {
      @Override
      public Aggregate register(Name name, Description description) {
        return new Time(span.register(name, description));
      }

      @Override
      public Aggregate edit(Name name, Description description) {
        return null;
      }

      @Override
      public Aggregate open() {
        return null;
      }

      @Override
      public Aggregate close() {
        return null;
      }

      @Override
      public Aggregate archive() {
        return null;
      }

      @Override
      public Aggregate reopen() {
        return null;
      }

      @Override
      public Service service(long version) {
        return new Providing();
      }
    }
  }

  sealed interface Service extends Forum<Aggregate>, Domain.Service permits Providing, Lifecycle {}

  F register(Forum.Name name, Forum.Description description);
  F edit(Forum.Name name, Forum.Description description);
  F open();
  F close();
  F archive();
  F reopen();

  default Aggregate aggregate() {
    return this instanceof Aggregate snapshot
      ? snapshot
      : null;
  }

  default Service transaction() {
    return this instanceof Service transaction ? transaction : null;
  }

  public static void main(String[] args) {
    final var forums = Forums.repository(null, null);
    Forum.with(new Forum.ID(UUID.randomUUID()))
      .register(new Name("ciao"), new Description("è un saluto"))
      .aggregate()
      .open()
      .transaction()
      .commit(forums::save);
  }
}

