package io.citadel.kernel.eventstore;

import io.citadel.kernel.eventstore.meta.*;
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
final class Sql implements Lookup {
  private final Vertx vertx;
  private final SqlClient client;

  Sql(Vertx vertx, SqlClient client) {
    this.vertx = vertx;
    this.client = client;
  }

  @Override
  public Future<Snapshot> find(final ID aggregateId, final Name aggregateName, final Version aggregateVersion) {
    return SqlTemplate.forQuery(client, """
        with aggregate as (
          select  aggregate_version as version
          from    event_store
          where   aggregate_id = #{aggregateId}
            and   lower(aggregate_name) = lower(#{aggregateName}) or #{aggregateName} is null
            and   aggregate_version <= #{aggregateVersion} or #{aggregateVersion} = 0
          order by aggregate_version desc
          limit 1
        )
        select  event_name, event_data, aggregate_id, aggregate_name, (select version from aggregate) as aggregate_version, timepoint
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
      .map(logs -> logs.collect(asSnapshotOf(aggregateId, aggregateName, aggregateVersion)))
      .map(snapshot -> snapshot.deserializes());
  }

  private AsSnapshot asSnapshotOf(ID aggregateId, Name aggregateName, Version aggregateVersion) {
    return new AsSnapshot(Aggregate.of(aggregateId, aggregateName, aggregateVersion));
  }

  private final class AsSnapshot implements Aggregator<Meta.Log, Snapshot[], Snapshot> {
    private final Aggregate aggregate;

    private AsSnapshot(Aggregate aggregate) {
      this.aggregate = aggregate;
    }

    @Override
    public Supplier<Snapshot[]> supplier() {
      return () -> new Snapshot[]{new Snapshot(vertx, client, aggregate)};
    }

    @Override
    public BiConsumer<Snapshot[], Meta.Log> accumulator() {
      return (snapshots, log) -> snapshots[0] = snapshots[0]
        .aggregate(log.aggregate())
        .append(log.event());
    }

    @Override
    public Function<Snapshot[], Snapshot> finisher() {
      return snapshots -> snapshots[0];
    }
  }
}
