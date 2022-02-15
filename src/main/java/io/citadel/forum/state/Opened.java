package io.citadel.forum.state;

import io.citadel.domain.message.Command;
import io.citadel.domain.source.EventStore;
import io.citadel.forum.Forum;
import io.citadel.forum.entity.Root;
import io.vertx.core.eventbus.EventBus;

public final class Opened implements Forum {
  private final EventBus eventBus;
  private final Root root;

  public Opened(EventBus eventBus, final Root root) {
    this.eventBus = eventBus;
    this.root = root;
  }

  @Override
  public Forum apply(final Command command) {
    return null;
  }

  @Override
  public void commit(Command command) {
    Forum.states.changed(eventBus, command.asEvents()).commit()
    eventBus.send(EventStore.operations.PERSIST, EventStore.events.)
  }
}
