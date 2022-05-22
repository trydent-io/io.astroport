package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.forum.handler.Events;

import java.util.Optional;

import static io.citadel.domain.forum.Forum.State.Archived;
import static io.citadel.domain.forum.Forum.State.Closed;
import static io.citadel.domain.forum.Forum.State.Open;
import static io.citadel.domain.forum.Forum.State.Registered;

public record Stage(State state) implements Forum {
  public Stage() {this(null);}

  @Override
  public Optional<Forum> assembly(final Event event) {
    return Optional.ofNullable(switch (event) {
      case Events.Registered it && state == null -> new Stage(Registered);
      case Events.Replaced it && state.is(Registered, Open) -> this;
      case Events.Opened it && state == Registered -> new Stage(Open);
      case Events.Closed it && state == Open -> new Stage(Closed);
      case Events.Archived it && state == Closed -> new Stage(Archived);
      default -> null;
    });
  }
}
