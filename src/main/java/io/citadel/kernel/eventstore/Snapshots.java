package io.citadel.kernel.eventstore;

import io.citadel.kernel.domain.Domain;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.templates.SqlTemplate;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;

@SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
record Snapshots(Vertx vertx, SqlClient client) implements Lookup {
  @Override
  public <ID extends Domain.ID<?>> Future<Snapshot<ID>> findSnapshot(final ID aggregateId, final String aggregateName, final long aggregateVersion) {
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
      .map(logs -> logs.collect(asSnapshot(aggregateId, aggregateName, aggregateVersion)));
  }

  private <ID extends Domain.ID<?>> AsSnapshot<ID> asSnapshot(ID aggregateId, String aggregateName, long aggregateVersion) {
    return new AsSnapshot<>(aggregateId, aggregateName, aggregateVersion);
  }

  @SuppressWarnings({"unchecked", "ConstantConditions"})
  private static final class AsSnapshot<ID extends Domain.ID<?>> implements Collector<Meta.Log, Snapshot<ID>[], Snapshot<ID>> {
    private static final Set<Characteristics> IdentityFinish = Set.of(IDENTITY_FINISH);
    private final ID aggregateId;
    private final String aggregateName;
    private final long aggregateVersion;

    public AsSnapshot(final ID aggregateId, final String aggregateName, long aggregateVersion) {
      this.aggregateId = aggregateId;
      this.aggregateName = aggregateName;
      this.aggregateVersion = aggregateVersion;
    }
    @Override
    public Supplier<Snapshot<ID>[]> supplier() {
      return () -> (Snapshot<ID>[]) new Object[]{new Snapshot<>(aggregateId, aggregateName, aggregateVersion, sqlClient, vertx)};
    }

    @Override
    public BiConsumer<Snapshot<ID>[], Meta.Log> accumulator() {
      return (snapshots, log) -> snapshots[0] = snapshots[0]
        .aggregate(log.aggregate())
        .append(log.event());
    }

    @Override
    public BinaryOperator<Snapshot<ID>[]> combiner() {
      return (prev, next) -> prev;
    }

    @Override
    public Function<Snapshot<ID>[], Snapshot<ID>> finisher() {
      return prototypes -> prototypes[0];
    }

    @Override
    public Set<Characteristics> characteristics() {
      return IdentityFinish;
    }
  }
}
