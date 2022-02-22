package io.citadel.forum.state;

import io.citadel.forum.Forum;
import io.citadel.forum.command.EditDescription;
import io.citadel.forum.command.EditName;
import io.citadel.forum.command.Open;
import io.citadel.forum.model.Model;
import io.citadel.kernel.domain.CommandException;

public final class RegisteredForum implements Forum {
  private final Model model;
  private final Event event;

  RegisteredForum(final Model model, final Event event) {
    this.model = model;
    this.event = event;
  }

  @Override
  public Forum apply(final Command command) {
    return switch (command) {
      case EditName edit -> apply(edit);
      case EditDescription edit -> apply(edit);
      case Open open -> apply(open);
      default -> throw new CommandException(command, Forum.NAME, getClass().getSimpleName());
    };
  }

  private Forum apply(final Open open) {
    return Forum.states.opened(model, event, open.asEvent());
  }

  private Forum apply(final EditDescription edit) {
    return Forum.states.opened(model.description(edit.description()), edit.asEvent());
  }

  private Forum apply(final EditName edit) {
    return Forum.states.opened(model.name(edit.name()), edit.asEvent());
  }
}
