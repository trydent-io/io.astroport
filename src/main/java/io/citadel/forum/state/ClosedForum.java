package io.citadel.forum.state;

import io.citadel.forum.Forum;
import io.citadel.forum.command.Reopen;
import io.citadel.forum.model.Model;
import io.citadel.kernel.media.Streamed;

public final class ClosedForum implements Forum, Streamed {
  private final Model model;
  private final Event[] events;

  ClosedForum(final Model model, final Event... events) {
    this.model = model;
    this.events = events;
  }

  @Override
  public Forum apply(final Command command) {
    return switch (command) {
      case Reopen reopen -> apply(reopen);
      default -> throwCommandException(command);
    };
  }

  private Forum apply(final Reopen reopen) {
    return Forum.states.opened(
      model
        .openedAt(reopen.at())
        .openedBy(reopen.memberID()),
      concat(events, reopen.asEvent())
    );
  }
}
