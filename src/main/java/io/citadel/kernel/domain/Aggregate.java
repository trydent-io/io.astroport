package io.citadel.kernel.domain;

import io.citadel.kernel.func.TryPredicate;
import io.citadel.kernel.func.TryFunction;
import io.citadel.kernel.func.TrySupplier;

public sealed interface Aggregate<ENTITY extends Record, EVENT extends Record> {
  static <ENTITY extends Record, EVENT extends Record> Aggregate<ENTITY, EVENT> node(ENTITY entity, Changes<EVENT> changes) {
    return new Node<>(entity, changes);
  }

  @SuppressWarnings("unchecked")
  static <ENTITY extends Record, EVENT extends Record> Aggregate<ENTITY, EVENT> empty() {
    return (Aggregate<ENTITY, EVENT>) Empty.Default;
  }

  static <ENTITY extends Record, EVENT extends Record> Aggregate<ENTITY, EVENT> zero(Changes<EVENT> changes) { return new Zero<>(changes); }

  default Aggregate<ENTITY, EVENT> when(TryPredicate<? super ENTITY> testify) { return this; }
  default Aggregate<ENTITY, EVENT> then(TryFunction<? super ENTITY, ? extends EVENT> apply) { return this; }
  default Aggregate<ENTITY, EVENT> then(TrySupplier<EVENT> supply) { return this; }
  default void commit() {}
}

enum Empty implements Aggregate<Record, Record> { Default }

final class Zero<ENTITY extends Record, EVENT extends Record> implements Aggregate<ENTITY, EVENT> {
  private final Changes<EVENT> changes;

  Zero(Changes<EVENT> changes) {
    this.changes = changes;
  }

  @Override
  public Aggregate<ENTITY, EVENT> then(TrySupplier<EVENT> supply) {
    return new Zero<>(changes.append(supply.get()));
  }
}

final class Node<ENTITY extends Record, EVENT extends Record> implements Aggregate<ENTITY, EVENT> {
  private final ENTITY entity;
  private final Changes<EVENT> changes;

  Node(ENTITY entity, Changes<EVENT> changes) {
    this.entity = entity;
    this.changes = changes;
  }

  @Override
  public Aggregate<ENTITY, EVENT> when(TryPredicate<? super ENTITY> testify) {
    return testify.test(entity) ? this : Aggregate.empty();
  }

  @Override
  public Aggregate<ENTITY, EVENT> then(TryFunction<? super ENTITY, ? extends EVENT> apply) {
    return new Node<>(entity, changes.append(apply.apply(entity)));
  }

  @Override
  public Aggregate<ENTITY, EVENT> then(TrySupplier<EVENT> supply) {
    return new Node<>(entity, changes.append(supply.get()));
  }

  @Override
  public void commit() {

  }
}
