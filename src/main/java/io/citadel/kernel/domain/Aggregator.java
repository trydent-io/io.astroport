package io.citadel.kernel.domain;

import io.citadel.kernel.eventstore.EventStore;

public interface Aggregator<AGGREGATE> {
  Aggregator<AGGREGATE> version(long version);
  <EVENT extends Record> Aggregator<AGGREGATE> event(EVENT event);
  AGGREGATE open(EventStore eventStore);
}
