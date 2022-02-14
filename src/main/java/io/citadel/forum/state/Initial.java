package io.citadel.forum.state;

import io.citadel.domain.message.Command;
import io.citadel.forum.Forum;
import io.citadel.forum.command.Open;
import io.vertx.core.eventbus.EventBus;

public final class Initial implements Forum {
  private final EventBus eventBus;

  public Initial(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  @Override
  public Forum apply(final Command command) {
    return switch (command) {
      case Open open -> Forum.states.opened();
    }
  }
}
