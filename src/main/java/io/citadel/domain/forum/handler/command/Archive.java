package io.citadel.domain.forum.handler.command;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.forum.handler.Commands;
import io.citadel.kernel.domain.Context;

public record Archive() implements Context.Behaviour<Forum.Transaction, Commands.Archive> {
  @Override
  public void be(Forum.Transaction aggregate, Commands.Archive behaviour, String by) {
    aggregate
      .log(Forum.event::archived)
      .commit(by);
  }
}
