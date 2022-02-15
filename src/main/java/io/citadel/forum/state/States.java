package io.citadel.forum.state;

import io.citadel.domain.message.Event;
import io.citadel.forum.Forum;
import io.citadel.forum.entity.Root;
import io.vertx.core.eventbus.EventBus;

public enum States {
  Defaults;

  public Forum initial(EventBus eventBus) { return new Initial(eventBus); }

  public Forum opened(EventBus eventBus, Root root) {
    return new Opened(eventBus, root);
  }

  public Forum closed() { return null; }

  public Forum changed(EventBus eventBus, Event... events) {}
}
