package io.citadel.context.forum.state;

import io.citadel.context.forum.Forum;
import io.citadel.context.forum.Events;
import io.citadel.context.forum.model.Model;
import io.citadel.shared.domain.Domain;
import io.citadel.shared.lang.Array;
import io.vertx.core.eventbus.EventBus;

import java.util.stream.Stream;

public enum States {
  Defaults;

  public Forum registered(final Forum.ID id, final Domain.Version version, final Model model, final Array<Forum.Event> events) {
    return new Aggregate(id, version, model, Forum.State.Registered, events);
  }

  public Forum open(final Forum.ID id, final Domain.Version version, final Model model, final Array<Forum.Event> events) {
    return new Aggregate(id, version, model, Forum.State.Open, events);
  }

  public Forum from(Forum.ID id, EventBus eventBus) {
    return new EventSourced(id, eventBus);
  }

  public Forum of(Forum.ID identity) {
    return new Aggregate(identity);
  }

  public Forum from(Forum.ID identity, Domain.Version version, Forum.Event... events) {
    return Stream.of(events).reduce(
        Forum.states.versioned(identity, version),
        (forum, event) -> switch (event) {
          case Events.Registered registered -> forum.register(registered.name(), registered.description(), registered.by());
          case Events.Opened opened -> forum.open(opened.by());
          case Events.Closed closed -> forum.close(closed.by());
          case Events.Edited.Name edit -> forum.edit(edit.name());
          case Events.Edited.Description edit -> forum.edit(edit.description());
          case Events.Reopened reopened -> forum.reopen();
        },
        (f, __) -> f);
  }

  public Forum versioned(Forum.ID identity, Domain.Version version) {
    return new Aggregate(identity, version);
  }

  public record Aggregate(Forum.ID id, Domain.Version version, Model model, Forum.State state, Array<Forum.Event> events) implements Forum {
    Aggregate(Forum.ID id) { this(id, Domain.Version.zero()); }
    Aggregate(Forum.ID id, Domain.Version version) { this(id, version, new Model(), State.Initial, Array.empty()); }

    @Override
    public boolean is(final State state) {
      return this.state.equals(state);
    }
  }

  public record EventSourced(Forum.ID id, EventBus eventBus) implements Forum {
    @Override
    public boolean is(State state) {
      return false;
    }
  }
}
