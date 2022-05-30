package io.citadel.kernel.eventstore.lookup;

import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.eventstore.Lookup;
import io.citadel.kernel.eventstore.Meta;
import io.citadel.kernel.eventstore.Prototype;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.templates.SqlTemplate;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;

@SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
public record Sql(SqlClient client) implements Lookup {
  @Override
  public <ID extends Domain.ID<?>> Future<Prototype<ID>> findPrototype(final ID aggregateId, final String aggregateName, final long aggregateVersion) {
    return SqlTemplate.forQuery(client, """
        with aggregate as (
          select  aggregate_version as version
          from    event_store
          where   aggregate_id = #{aggregateId}
            and   lower(aggregate_name) = lower(#{aggregateName}) or #{aggregateName} is null
            and   aggregate_version <= #{aggregateVersion} or #{aggregateVersion} = -1
          order by aggregate_version desc
          limit 1
        )
        select  id, event_name, event_data, aggregate_id, aggregate_name, (select version from aggregate) as aggregate_version, persisted_at
        from    event_store
        where   aggregate_id = #{aggregateId} and (lower(aggregate_name) = lower(#{aggregateName}) or #{aggregateName} is null)
        """)
      .mapTo(row -> row)
      .execute(
        Map.of(
          "aggregateId", aggregateId.toString(),
          "aggregateName", aggregateName,
          "aggregateVersion", aggregateVersion
        )
      )
      .map(Meta::fromRows)
      .map(Meta::stream)
      .map(logs -> logs.collect(toPrototype(aggregateId, aggregateName, aggregateVersion)));
  }

  private <ID extends Domain.ID<?>> ToPrototype<ID> toPrototype(ID aggregateId, String aggregateName, long aggregateVersion) {
    return new ToPrototype<>(aggregateId, aggregateName, aggregateVersion);
  }

  @SuppressWarnings({"unchecked", "ConstantConditions"})
  private static final class ToPrototype<ID extends Domain.ID<?>> implements Collector<Meta.Log, Prototype<ID>[], Prototype<ID>> {
    private static final Set<Characteristics> IdentityFinish = Set.of(IDENTITY_FINISH);
    private final ID aggregateId;
    private final String aggregateName;
    private final long aggregateVersion;

    public ToPrototype(final ID aggregateId, final String aggregateName, long aggregateVersion) {
      this.aggregateId = aggregateId;
      this.aggregateName = aggregateName;
      this.aggregateVersion = aggregateVersion;
    }

    private Stream<Meta.Event> append(Stream<Meta.Event> events, Meta.Event event) {
      return Stream.concat(events, Stream.of(event));
    }

    @Override
    public Supplier<Prototype<ID>[]> supplier() {
      return () -> (Prototype<ID>[]) new Object[]{new Prototype<>(aggregateId, aggregateName, aggregateVersion)};
    }

    @Override
    public BiConsumer<Prototype<ID>[], Meta.Log> accumulator() {
      return (prototypes, log) -> prototypes[0] = prototypes[0].aggregate(log.aggregate()).append(log.event());
    }

    @Override
    public BinaryOperator<Prototype<ID>[]> combiner() {
      return (prev, next) -> prev;
    }

    @Override
    public Function<Prototype<ID>[], Prototype<ID>> finisher() {
      return prototypes -> prototypes[0];
    }

    @Override
    public Set<Characteristics> characteristics() {
      return IdentityFinish;
    }
  }
}
