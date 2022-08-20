package io.citadel.kernel.domain;

import io.citadel.kernel.func.TryPredicate;
import io.citadel.kernel.func.TryFunction;
import io.citadel.kernel.func.TrySupplier;
import io.vertx.core.Future;

public sealed interface Aggregate<ENTITY extends Record, EVENT> {
  static <ENTITY extends Record, EVENT> Aggregate<ENTITY, EVENT> node(ENTITY entity, Changes<EVENT> changes) {
    return new Node<>(entity, changes);
  }

  @SuppressWarnings("unchecked")
  static <ENTITY extends Record, EVENT> Aggregate<ENTITY, EVENT> empty() {
    return (Aggregate<ENTITY, EVENT>) Empty.Companion;
  }

  default Aggregate<ENTITY, EVENT> when(TryPredicate<? super ENTITY> testify) { return this; }
  default <DOMAIN_EVENT extends EVENT> Aggregate<ENTITY, EVENT> then(TryFunction<? super ENTITY, ? extends DOMAIN_EVENT> apply) { return this; }
  default <DOMAIN_EVENT extends EVENT> Aggregate<ENTITY, EVENT> then(TrySupplier<DOMAIN_EVENT> supply) { return this; }
  default Future<Aggregate<ENTITY, EVENT>> push() { return null; }
}

enum Empty implements Aggregate<Record, Object> {Companion}

final class Node<ENTITY extends Record, EVENT> implements Aggregate<ENTITY, EVENT> {
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
  public <DOMAIN_EVENT extends EVENT> Aggregate<ENTITY, EVENT> then(TryFunction<? super ENTITY, ? extends DOMAIN_EVENT> apply) {
    return new Node<>(entity, changes.add(apply.apply(entity)));
  }

  @Override
  public <DOMAIN_EVENT extends EVENT> Aggregate<ENTITY, EVENT> then(TrySupplier<DOMAIN_EVENT> supply) {
    return new Node<>(entity, changes.add(supply.get()));
  }

  @Override
  public Future<Aggregate<ENTITY, EVENT>> push() {
    return changes.commit().map(Aggregate.empty());
  }
}
