package io.citadel.eventstore;

public enum Operations {
  Defaults;

  public final String FIND_BY = "eventStore.findBy";
  public final String PERSIST = "eventStore.persist";
}
