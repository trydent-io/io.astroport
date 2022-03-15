package io.citadel.context.forum.state;

import io.citadel.context.forum.Forum;
import io.citadel.context.forum.event.Events;
import io.citadel.context.forum.model.Model;
import io.vertx.core.eventbus.EventBus;

import java.util.stream.Stream;

public enum States {
  Defaults;

  public Forum registered(final Forum.ID id, final long version, final Model model, final Stream<Forum.Event> events) {
    return new Aggregate(id, version, model, Forum.State.Registered, events);
  }

  public Forum open(final Forum.ID id, final long version, final Model model, final Stream<Forum.Event> events) {
    return new Aggregate(id, version, model, Forum.State.Open, events);
  }

  public Forum close(final Forum.ID id, final long version, final Model model, final Stream<Forum.Event> events) {
    return  new Aggregate(id, version, model, Forum.State.Closed, events);
  }

  public Forum from(Forum.ID id, EventBus eventBus) {
    return new EventSourced(id, eventBus);
  }

  public Forum of(Forum.ID identity) {
    return new Aggregate(identity);
  }

  public Forum identity(Forum.ID id, long version) {
    return new Aggregate(id, version);
  }

  public record Aggregate(Forum.ID id, long version, Model model, Forum.State state, Stream<Forum.Event> events) implements Forum {
    Aggregate(Forum.ID id) { this(id, 0); }
    Aggregate(Forum.ID id, long version) { this(id, version, new Model(), State.Initial, Stream.empty()); }

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
