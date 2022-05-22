package io.citadel.domain.forum.handler.command;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.forum.handler.Commands;
import io.citadel.kernel.domain.Actor;

public record Open() implements Actor.Behaviour<Forum.Aggregate, Commands.Open> {
  @Override
  public void be(Forum.Aggregate aggregate, Commands.Open behaviour, String by) {
    aggregate
      .asserts(model -> model.details().description().value().isEmpty())
      .notify(Forum.events::opened)
      .submit();
  }
}
