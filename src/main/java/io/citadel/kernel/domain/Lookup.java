package io.citadel.kernel.domain;

import io.citadel.kernel.eventstore.EventStore;
import io.citadel.kernel.eventstore.event.Entity;
import io.citadel.kernel.eventstore.event.Audit;
import io.citadel.kernel.eventstore.event.Event;
import io.citadel.kernel.eventstore.metadata.MetaAggregate.Last;
import io.citadel.kernel.eventstore.metadata.MetaAggregate.Zero;
import io.citadel.kernel.func.TryBiFunction;
import io.citadel.kernel.func.TrySupplier;
import io.citadel.kernel.lang.stream.Streamer;
import io.citadel.kernel.vertx.Task;
import io.vertx.core.Future;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

public sealed interface Lookup<A> {
  static <A> Lookup<A> aggregate(EventStore eventStore, String name, BiFunction<? super EventStore, ? super Zero, ? extends A> zero, BiFunction<? super EventStore, ? super Last, ? extends A> last) {
    return new Once<>(new Repository<>(eventStore, Entity.name(name), zero, last));
  }

  Future<A> aggregate(String id);

  final class Repository<A> implements Lookup<A>, Task, Streamer<Audit> {
    private final EventStore eventStore;
    private final Entity.Name name;
    private final TrySupplier<A> zero;
    private final TryBiFunction<? super A, ? super Event, ? extends A> last;

    private Repository(EventStore eventStore, Entity.Name name, TrySupplier<A> zero, TryBiFunction<? super A, ? super Event, ? extends A> last) {
      this.eventStore = eventStore;
      this.name = name;
      this.zero = zero;
      this.last = last;
    }

    @Override
    public Future<A> aggregate(String id) {
      return eventStore.restore(Entity.id(id), name)
        .map(events -> events.collect(
            folding(zero, (acc, event) ->
              switch (event) {
                case Audit.Identity ignored -> acc;
                case Audit.Applied it -> last.apply(acc, it.event());
              }
            )
          )
        );
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
