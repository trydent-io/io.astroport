package io.citadel.domain.forum.handler.command;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.forum.handler.Commands;
import io.citadel.kernel.domain.Actor;

public record Open() implements Actor.Behaviour<Forum.Transaction, Commands.Open> {
  @Override
  public void be(Forum.Transaction aggregate, Commands.Open behaviour, String by) {
    aggregate
      .has(model -> model.details().description().value().isEmpty())
      .log(Forum.event::opened)
      .commit();
  }
}
