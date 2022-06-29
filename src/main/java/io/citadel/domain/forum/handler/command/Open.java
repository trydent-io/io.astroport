package io.citadel.domain.forum.handler.command;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.forum.handler.Commands;

public record Open() implements Context.Behaviour<Forum.Transaction, Commands.Open> {
  @Override
  public void be(Forum.Transaction aggregate, Commands.Open behaviour, String by) {
    aggregate
      .has(model -> model.details().description().value().isEmpty())
      .log(Forum.event::opened)
      .commit();
  }
}
