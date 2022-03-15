package io.citadel.shared.context.repository;

import io.citadel.shared.context.Domain;

import java.util.function.UnaryOperator;

public record Root<A extends Domain.Aggregate<A, M, S>, I extends Domain.ID<?>, M extends Record & Domain.Model, S extends Domain.State<?>>(I id, long version, M model, S state) implements Domain.Aggregate<A, M, S> {
  @Override
  public Domain.Aggregate<A, M, S> nextIf(final S state, final S next, final UnaryOperator<M> model) {
    return this.state.equals(state)
      ? new Root<>(id, version, model.apply(this.model), next)
      : this;
  }
}
