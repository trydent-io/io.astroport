package io.citadel.forum.state;

import io.citadel.domain.message.Command;
import io.citadel.domain.message.CommandException;
import io.citadel.forum.Forum;
import io.citadel.forum.command.Open;
import io.citadel.forum.entity.Root;
import io.vertx.core.eventbus.EventBus;

public final class Initial implements Forum {
  private final EventBus eventBus;

  public Initial(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  @Override
  public Forum apply(final Command command) {
    return switch (command) {
      case Open open -> apply(open);
      default -> throw new CommandException(command, "Forum", "initial");
    };
  }

  private Forum apply(final Open open) {
    return Forum.states.opened(eventBus, Root.with(it -> {
      it.openedBy = open.by();
      it.title = open.title();
      it.description = open.description();
      it.openedAt = open.at();
    }));
  }
}
