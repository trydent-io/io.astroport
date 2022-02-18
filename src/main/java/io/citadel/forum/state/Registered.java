package io.citadel.forum.state;

import io.citadel.forum.Forum;
import io.citadel.forum.command.Open;
import io.citadel.forum.model.Model;

final class Registered implements Forum {
  private final Model model;
  private final Event event;

  Registered(final Model model, final Event event) {
    this.model = model;
    this.event = event;
  }

  @Override
  public Forum apply(final Command command) {
    return switch (command) {
      case Open open -> Forum.states.opened(model, );
    };
  }
}
