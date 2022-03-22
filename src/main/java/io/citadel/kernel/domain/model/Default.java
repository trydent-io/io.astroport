package io.citadel.kernel.domain.model;

import io.citadel.kernel.domain.Domain;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public final class Default<S extends Domain.State<?>, E extends Domain.Entity<S, E>> implements Domain.Entity<S, E> {
  @Override
  public E onDefault(final S next, final Supplier<? extends E> supplier) {
    return new Next<>(next, supplier.get());
  }

  @Override
  public E on(final S state, final S next, final UnaryOperator<E> operator) {
    return null;
  }
}

final class Next<S extends Domain.State<?>, E extends Domain.Entity<S, E>> implements Domain.Entity<S, E> {
  private final S state;
  private final E entity;

  Next(final S state, final E entity) {
    this.state = state;
    this.entity = entity;
  }

  @Override
  public E onDefault(final S next, final Supplier<? extends E> supplier) {
    return null;
  }

  @Override
  public E on(final S state, final S next, final UnaryOperator<E> operator) {
    return null;
  }
}
