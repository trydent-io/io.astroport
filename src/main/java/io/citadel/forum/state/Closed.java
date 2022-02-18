package io.citadel.forum.state;

import io.citadel.forum.Forum;
import io.citadel.forum.model.Model;
import io.citadel.kernel.domain.CommandException;

public final class Closed implements Forum {
  private final Model model;
  private final Event[] events;

  public Closed(final Model model, final Event[] events) {
    this.model = model;
    this.events = events;
  }

  @Override
  public Forum apply(final Command command) {
    throw new CommandException(command, Forum.name, "Closed");
  }
}
