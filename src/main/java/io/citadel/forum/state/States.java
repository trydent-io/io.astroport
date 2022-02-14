package io.citadel.forum.state;

import io.citadel.domain.message.Event;
import io.citadel.forum.Forum;
import io.vertx.core.eventbus.EventBus;

public enum States {
  Defaults;

  public Forum initial(EventBus eventBus) { return new Initial(eventBus); }

  public Forum opened() {
    return null;
  }

  public Forum closed() { return null; }

  public Forum changed(EventBus eventBus, Event... events) {}
}
