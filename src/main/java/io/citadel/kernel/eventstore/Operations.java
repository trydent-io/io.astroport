package io.citadel.kernel.eventstore;

public enum Operations {
  Defaults;

  public final String RESTORE = "eventstore.find-by";
  public final String PERSIST = "eventstore.persist";
}
