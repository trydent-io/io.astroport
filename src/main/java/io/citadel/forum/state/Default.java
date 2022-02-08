package io.citadel.forum.state;

import io.citadel.domain.message.Command;
import io.citadel.forum.Forum;
import io.citadel.forum.command.Open;

public enum Default implements Forum {
  Initial;

  @Override
  public Forum apply(final Command command) {
    return switch (command) {
      case Open open -> Forum.states.opened()
    }
  }
}
