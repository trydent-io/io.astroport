package io.citadel.kernel.domain;

import io.citadel.kernel.eventstore.Audit;
import io.citadel.kernel.eventstore.EventStore;
import io.citadel.kernel.func.TryBiFunction;
import io.citadel.kernel.func.TryFunction;
import io.citadel.kernel.func.TrySupplier;
import io.citadel.kernel.lang.stream.Streamer;
import io.citadel.kernel.vertx.Task;
import io.vertx.core.Future;

import java.util.concurrent.atomic.AtomicReference;

public interface Aggregates<AGGREGATE, ID, ENTITY extends Record, EVENT> {

  static <AGGREGATE, ID, ENTITY extends Record, EVENT> Aggregates<AGGREGATE, ID, ENTITY, EVENT> repository(
    String name,
    EventStore eventStore,
    TryFunction<? super Audit.Event, ? extends EVENT> eventuate,
    TrySupplier<ENTITY> suppl, TryBiFunction<? super ENTITY, ? super EVENT, ? extends ENTITY> hydrant
  ) {
    return new Lookup<>(eventStore);
  }

  Future<AGGREGATE> aggregate(ID id);

  final class Lookup<AGGREGATE, ID, ENTITY extends Record, EVENT> implements Aggregates<AGGREGATE, ID, ENTITY, EVENT>, Task, Streamer<EVENT> {
    private final String name;
    private final EventStore eventStore;
    private final TryFunction<? super Audit.Event, ? extends EVENT> eventuate;
    private final TrySupplier<ENTITY> identity;
    private final TryBiFunction<? super ENTITY, ? super EVENT, ? extends ENTITY> hydrant;

    public Lookup(String name, EventStore eventStore, TryFunction<? super Audit.Event, ? extends EVENT> eventuate, TrySupplier<ENTITY> identity, TryBiFunction<? super ENTITY, ? super EVENT, ? extends ENTITY> hydrant) {
      this.name = name;
      this.eventStore = eventStore;
      this.eventuate = eventuate;
      this.identity = identity;
      this.hydrant = hydrant;
    }

    private record Domain<EVENT>(Audit.Entity entity, EVENT event) {
      private static <EVENT> Domain<EVENT> event(Audit audit, TryFunction<? super Audit.Event, ? extends EVENT> eventuate) {
        return new Domain<>(audit.entity(), eventuate.apply(audit.event()));
      }
    }

    @Override
    public Future<AGGREGATE> aggregate(ID id) {
      return eventStore
        .restore(Audit.Entity.zero(id.toString(), name))
        .map(audits -> audits.map(audit -> Domain.event(audit, eventuate)))
        .map(domainEvents -> domainEvents.collect(folding(identity, Domain::event, hydrant)))
        .map(null);
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
