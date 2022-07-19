package io.citadel.kernel.domain;

import io.citadel.kernel.eventstore.EventStore;
import io.citadel.kernel.eventstore.event.ID;
import io.citadel.kernel.eventstore.metadata.MetaAggregate.Last;
import io.citadel.kernel.eventstore.metadata.MetaAggregate.Zero;
import io.citadel.kernel.eventstore.event.Name;
import io.vertx.core.Future;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

public sealed interface Lookup<A> {
  static <A> Lookup<A> aggregate(EventStore pool, String name, BiFunction<? super EventStore, ? super Zero, ? extends A> zero, BiFunction<? super EventStore, ? super Last, ? extends A> last) {
    return new Once<>(new Aggregate<>(pool, Name.of(name), zero, last));
  }

  Future<A> aggregate(String id);

  final class Aggregate<A> implements Lookup<A> {
    private final EventStore pool;
    private final Name name;
    private final BiFunction<? super EventStore, ? super Zero, ? extends A> zero;
    private final BiFunction<? super EventStore, ? super Last, ? extends A> last;

    private Aggregate(EventStore pool, Name name, BiFunction<? super EventStore, ? super Zero, ? extends A> zero, BiFunction<? super EventStore, ? super Last, ? extends A> last) {
      this.pool = pool;
      this.name = name;
      this.zero = zero;
      this.last = last;
    }

    @Override
    public Future<A> aggregate(String id) {
      return pool.query(ID.of(id), name)
        .map(aggregate -> switch (aggregate) {
          case Zero it -> zero.apply(pool, it);
          case Last it -> last.apply(pool, it);
          default -> throw new IllegalStateException("Unexpected value: " + aggregate);
        });
    }
  }

  final class Once<A> implements Lookup<A> {
    private final Lookup<A> lookup;
    private final AtomicReference<Future<A>> reference;

    private Once(Lookup<A> lookup) {
      this.lookup = lookup;
      this.reference = new AtomicReference<>();
    }

    @Override
    public Future<A> aggregate(String id) {
      return reference.compareAndExchange(null, lookup.aggregate(id));
    }
  }
}
