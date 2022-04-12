package io.citadel.domain.forum.handler.command;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.forum.aggregate.Forums;
import io.citadel.domain.forum.handler.Commands;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.domain.Headers;
import io.vertx.core.eventbus.Message;

public record Open(Forums forums) implements Domain.Handler<Commands.Open> {
  @Override
  public void handle(final Message<Commands.Open> message, final Commands.Open open, final Headers headers) {
    final var forumId = headers.aggregateId(Forum.ID::new).orElseThrow();

    forums
      .findBy(forumId, Forum.State.Open)
      .map(Forum::open)
      .compose(aggregate -> aggregate.commit(forums::persist))
      .onSuccess(aggregate -> message.reply("Forum with Id %s has been registered".formatted(forumId)))
      .onFailure(throwable -> message.fail(406, "Can't register Forum with Id %s".formatted(forumId)));
  }
}
