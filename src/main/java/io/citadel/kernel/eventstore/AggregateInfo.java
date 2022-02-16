package io.citadel.kernel.eventstore;

import jakarta.persistence.Embeddable;

import java.util.UUID;
import java.util.function.Consumer;

@Embeddable
public class AggregateInfo {
  public UUID id;
  public String name;
  public UUID revision;

  public static AggregateInfo with(Consumer<AggregateInfo> consumer) {
    final var aggregate = new AggregateInfo();
    consumer.accept(aggregate);
    return aggregate;
  }
}
