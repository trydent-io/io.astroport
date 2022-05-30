package io.citadel.kernel.eventstore;

import io.citadel.kernel.domain.Domain;
import io.vertx.core.Future;


public interface Lookup {
  <ID extends Domain.ID<?>> Future<Timeline<ID>> findTimeline(ID aggregateId, String aggregateName, long aggregateVersion);
  default <ID extends Domain.ID<?>> Future<Timeline<ID>> findTimeline(ID aggregateId, String aggregateName) {
    return findTimeline(aggregateId, aggregateName, -1);
  }
  default <ID extends Domain.ID<?>> Future<Timeline<ID>> findTimeline(ID aggregateId) {
    return findTimeline(aggregateId, null);
  }
}
