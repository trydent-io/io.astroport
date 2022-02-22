package io.citadel.forum.state;

import io.citadel.forum.Forum;
import io.citadel.forum.model.Model;
import io.citadel.forum.command.Close;
import io.citadel.kernel.domain.CommandException;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Stream.concat;

public final class OpenedForum implements Forum {
  private static final String STATE = "Opened";

  private final Model model;
  private final List<Event> events;

  OpenedForum(final Model model, final Event... events) {
    this.model = model;
    this.events = List.of(events);
  }

  @Override
  public Forum apply(final Command command) {
    return switch (command) {
      case Close close -> apply(close);
      default -> throw new CommandException(command, Forum.NAME, STATE);
    };
  }

  private Forum apply(Close close) {
    return Forum.states.closed(
      model
        .closedAt(close.at())
        .closedBy(close.by()),
      (Event[]) concat(events.stream(), Stream.of(close.asEvent())).toArray()
    );
  }
}
