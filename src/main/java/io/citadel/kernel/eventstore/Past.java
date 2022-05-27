package io.citadel.kernel.eventstore;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;

public final class Past implements Collector<Feed.Log, Timeline[], Timeline> {
  private static final Set<Characteristics> IdentityFinish = Set.of(IDENTITY_FINISH);
  private final String aggregateId;
  private final String aggregateName;

  public Past(final String aggregateId, final String aggregateName) {
    this.aggregateId = aggregateId;
    this.aggregateName = aggregateName;
  }

  private Stream<Feed.Event> append(Stream<Feed.Event> events, Feed.Event event) {
    return Stream.concat(events, Stream.of(event));
  }

  @Override
  public Supplier<Timeline[]> supplier() {
    return () -> new Timeline[]{new Timeline(new Feed.Aggregate(aggregateId, aggregateName), Stream.empty())};
  }

  @Override
  public BiConsumer<Timeline[], Feed.Log> accumulator() {
    return (timelines, log) -> timelines[0] = timelines[0].aggregate(log.aggregate()).append(log.event());
  }

  @Override
  public BinaryOperator<Timeline[]> combiner() {
    return (timelines, timelines2) -> timelines;
  }

  @Override
  public Function<Timeline[], Timeline> finisher() {
    return timelines -> timelines[0];
  }

  @Override
  public Set<Characteristics> characteristics() {
    return IdentityFinish;
  }

  public static Collector<Feed.Log, Timeline[], Timeline> toTimelineOf(Feed.Aggregate aggregate) {
    return new Past(aggregate.id(), aggregate.name());
  }
}
