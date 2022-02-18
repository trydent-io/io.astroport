package io.citadel.forum.state;

import io.citadel.forum.Forum;
import io.citadel.forum.model.Model;
import io.citadel.forum.command.Close;
import io.citadel.kernel.domain.CommandException;

public final class Opened implements Forum {
  private final Model model;
  private final Event event;

  public Opened(final Model model, final Event event) {
    this.model = model;
    this.event = event;
  }

  @Override
  public Forum apply(final Command command) {
    return switch (command) {
      case Close close -> apply(close);
      default -> throw new CommandException(command, Forum.name, "Opened");
    };
  }

  private Forum apply(Close close) {
    return Forum.states.closed(
      model
        .closedAt(close.at())
        .closedBy(close.by()),
      close.asEvent()
    );
  }
}
