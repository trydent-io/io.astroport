package io.citadel.domain.forum.handler.command;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.forum.handler.Commands;
import io.citadel.kernel.domain.Context;

public record Close() implements Context.Behaviour<Forum.Transaction, Commands.Close> {

  @Override
  public void be(Forum.Transaction aggregate, Commands.Close close, String by) {

  }
}
