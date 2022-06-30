package io.citadel.kernel.domain;

import io.citadel.kernel.eventstore.EventPool;
import io.citadel.kernel.eventstore.metadata.ID;
import io.citadel.kernel.eventstore.metadata.MetaAggregate.Last;
import io.citadel.kernel.eventstore.metadata.MetaAggregate.Zero;
import io.citadel.kernel.eventstore.metadata.Name;
import io.vertx.core.Future;

import java.util.function.BiFunction;

public sealed interface Aggregate<A> {
  static <A> Aggregate<A> root(EventPool pool, String name, BiFunction<? super EventPool, ? super Zero, ? extends A> zero, BiFunction<? super EventPool, ? super Last, ? extends A> last) {
    return new Root<>(pool, Name.of(name), zero, last);
  }

  Future<A> aggregate(String id);

  final class Root<A> implements Aggregate<A> {
    private final EventPool pool;
    private final Name name;
    private final BiFunction<? super EventPool, ? super Zero, ? extends A> zero;
    private final BiFunction<? super EventPool, ? super Last, ? extends A> last;

    private Root(EventPool pool, Name name, BiFunction<? super EventPool, ? super Zero, ? extends A> zero, BiFunction<? super EventPool, ? super Last, ? extends A> last) {
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
}
