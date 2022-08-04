package io.citadel.kernel.domain;

import io.citadel.kernel.eventstore.EventStore;
import io.citadel.kernel.eventstore.Audit;
import io.citadel.kernel.eventstore.metadata.MetaAggregate.Last;
import io.citadel.kernel.eventstore.metadata.MetaAggregate.Zero;
import io.citadel.kernel.func.TryBiFunction;
import io.citadel.kernel.func.TryFunction;
import io.citadel.kernel.lang.stream.Streamer;
import io.citadel.kernel.vertx.Task;
import io.vertx.core.Future;

import java.util.concurrent.atomic.AtomicReference;

public sealed interface Aggregates<AGGREGATE extends Aggregate<?, ?>> {
  static <AGGREGATE extends Aggregate<?, ?>> Aggregates<AGGREGATE> lookup(EventStore eventStore) {
    return new Lookup<>(eventStore);
  }

  <ENTITY extends Record> Future<AGGREGATE> aggregate(Audit.Entity entity, TryBiFunction<? super ENTITY, ? super Audit.Event, ? extends ENTITY> func);

  final class Lookup<AGGREGATE extends Aggregate<?, ?>> implements Aggregates<AGGREGATE>, Task, Streamer<Audit> {
    private final EventStore eventStore;
    private Lookup(EventStore eventStore) {
      this.eventStore = eventStore;
    }

    @Override
    public Future<AGGREGATE> find(String id) {
      return eventStore.restore(Audit.Entity.zero(id, name)).map(
        events -> events.collect(
          folding(zero, (acc, audit) -> audit.event() == null
            ? acc
            : last.apply(acc, audit.event())
          )
        )
      );
    }

    @Override
    public <ENTITY extends Record> Future<AGGREGATE> aggregate(Audit.Entity entity, TryBiFunction<? super ENTITY, ? super Audit.Event, ? extends ENTITY> func) {
      return eventStore.restore(entity).map(
        events -> events.collect(
          folding(
            () -> null,
            (acc, audit) -> audit.event() == null ? acc : acc.next(it -> func.apply(audit.event()))
            )
        )
      );
    }
  }

  final class Once<A> implements Aggregates<A> {
    private final Aggregates<A> aggregates;
    private final AtomicReference<Future<A>> reference;

    private Once(Aggregates<A> aggregates) {
      this.aggregates = aggregates;
      this.reference = new AtomicReference<>();
    }

    @Override
    public Future<A> find(String id) {
      return reference.compareAndExchange(null, aggregates.find(id));
    }
  }
}
