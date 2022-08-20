package io.citadel.kernel.domain;

import io.citadel.kernel.eventstore.Audit;
import io.citadel.kernel.eventstore.EventStore;
import io.citadel.kernel.vertx.Task;
import io.vertx.core.Future;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public interface Repository<ID, ENTITY extends Record, EVENT> {
  static <ID, ENTITY extends Record, EVENT, STATE extends Enum<STATE> & State<STATE, EVENT>> Repository<ID, ENTITY, EVENT> lookup(
    String name,
    EventStore eventStore,
    Archetype<ID, ENTITY, EVENT, STATE> archetype
  ) {
    return new Lookup<>(name, eventStore, archetype);
  }

  Future<Aggregate<ENTITY, EVENT>> pull(ID id);

  final class Lookup<ID, ENTITY extends Record, EVENT, STATE extends Enum<STATE> & State<STATE, EVENT>> implements Repository<ID, ENTITY, EVENT>, Task {
    private static final Set<Collector.Characteristics> IdentityFinish = Set.of(Collector.Characteristics.IDENTITY_FINISH);

    private final String name;
    private final EventStore eventStore;
    private final Archetype<ID, ENTITY, EVENT, STATE> archetype;

    private Lookup(String name, EventStore eventStore, Archetype<ID, ENTITY, EVENT, STATE> archetype) {
      this.name = name;
      this.eventStore = eventStore;
      this.archetype = archetype;
    }

    @Override
    public Future<Aggregate<ENTITY, EVENT>> pull(ID id) {
      return eventStore
        .restore(Audit.Entity.with(id.toString(), name))
        .map(audits -> audits.collect(new Aggregating(id)));
    }


    final class Aggregating implements Collector<Audit, ENTITY, Aggregate<ENTITY, EVENT>> {
      private STATE state;
      private ENTITY entity;
      private Audit audit;

      private Aggregating(ID id) {
        this.state = archetype.state();
        this.entity = archetype.initialize(id);
        this.audit = null;
      }

      @Override
      public Supplier<ENTITY> supplier() {
        return () -> entity;
      }

      @Override
      public BiConsumer<ENTITY, Audit> accumulator() {
        return (entity, audit) -> {
          final var event = archetype.transform(audit.event());
          this.state = state.transit(event);
          this.entity = archetype.accumulate(entity, event);
          this.audit = audit;
        };
      }

      @Override
      public BinaryOperator<ENTITY> combiner() {
        return (entity, entity2) -> entity;
      }

      @Override
      public Function<ENTITY, Aggregate<ENTITY, EVENT>> finisher() {
        return entity -> Aggregate.node(entity, Changes.local(eventStore, audit.entity(), state));
      }

      @Override
      public Set<Characteristics> characteristics() {
        return IdentityFinish;
      }
    }
  }
}
