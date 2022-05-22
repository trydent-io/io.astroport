package io.citadel.domain.forum.handler.command;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.forum.handler.Commands;
import io.citadel.kernel.domain.Actor;

public record Archive() implements Actor.Behaviour<Forum.Aggregate, Commands.Archive> {
  @Override
  public void be(Forum.Aggregate aggregate, Commands.Archive behaviour, String by) {
    aggregate
      .notify(Forum.events::archived)
      .submit(by);
  }
}
