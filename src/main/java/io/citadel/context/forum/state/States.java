package io.citadel.context.forum.state;

import io.citadel.context.forum.Forum;
import io.citadel.context.forum.model.Model;
import io.citadel.shared.context.Domain;
import io.citadel.shared.context.repository.Root;

import java.util.function.UnaryOperator;
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

  public Forum of(Forum.ID identity) {
    return new Aggregate(new Root<>(identity, 0, new Forum.Model(), Forum.State.Initial));
  }

  public Forum identity(Forum.ID identity, long version) {
    return new Aggregate(new Root<>(identity, 0, new Forum.Model(), Forum.State.Initial));
  }

  public record Aggregate(Domain.Aggregate<Forum, Forum.Model, Forum.State> root) implements Forum {
    @Override
    public boolean is(final State state) {
      return root.is(state);
    }

    @Override
    public Domain.Aggregate<Forum, Model, State> nextIf(final State state, final State next, final UnaryOperator<Model> model) {
      return root.nextIf(state, next, model);
    }
  }
}
