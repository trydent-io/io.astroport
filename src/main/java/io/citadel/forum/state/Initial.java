package io.citadel.forum.state;

import io.citadel.forum.Forum;
import io.citadel.forum.command.Register;
import io.citadel.forum.model.Model;
import io.citadel.kernel.domain.CommandException;

public final class Initial implements Forum {
  private final ID identity;

  Initial(final ID identity) {this.identity = identity;}

  @Override
  public Forum apply(final Command command) {
    return switch (command) {
      case Register register -> Forum.states.registered(Model.with(identity), register.asEvent());
      default -> throw new CommandException(command, Forum.NAME, "Initial");
    };
  }
}
