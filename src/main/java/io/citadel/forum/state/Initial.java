package io.citadel.forum.state;

import io.citadel.forum.Forum;
import io.citadel.forum.model.Model;
import io.citadel.forum.command.Open;
import io.citadel.kernel.domain.CommandException;

public final class Initial implements Forum {
  private final ID identity;

  Initial(final ID identity) {this.identity = identity;}

  @Override
  public Forum apply(final Command command) {
    return switch (command) {
      case Open open -> Forum.states.opened(Model.with(identity), open.asEvent());
      default -> throw new CommandException(command, Forum.name, "Initial");
    };
  }
}
