package io.citadel.domain.forum.handler.command;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.forum.handler.Commands;

public record Open() implements Forum.Behaviour<Commands.Open> {
  @Override
  public void be(Forum.Aggregate aggregate, Commands.Open behaviour, String by) {
    aggregate.open().submit(by);
  }
}
