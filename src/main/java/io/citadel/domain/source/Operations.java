package io.citadel.domain.source;

public enum Operations {
  Defaults;

  public final String RESTORE = "eventstore.restore";
  public final String PERSIST = "eventstore.persist";
}
