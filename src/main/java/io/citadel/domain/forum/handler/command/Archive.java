package io.citadel.domain.forum.handler.command;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.forum.handler.Commands;
import io.citadel.kernel.domain.Actor;

public record Archive() implements Actor.Behaviour<Forum.Transaction, Commands.Archive> {
  @Override
  public void be(Forum.Transaction aggregate, Commands.Archive behaviour, String by) {
    aggregate
      .log(Forum.events::archived)
      .commit(by);
  }
}
