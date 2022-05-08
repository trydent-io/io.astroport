package io.citadel.domain.forum.handler.command;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.forum.aggregate.Forums;
import io.citadel.domain.forum.handler.Commands;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.domain.Headers;
import io.vertx.core.eventbus.Message;

public record Open(Forums forums) implements Domain.Handler<Commands.Open> {
  public void handle(final Message<Commands.Open> message, final Commands.Open open, final Headers headers) {
    forums
      .forum(forumId)
      .compose(Forum::open)
      .compose(Aggregate::submit)
      .onSuccess(aggregate -> message.reply("Forum with Id %s has been opened".formatted(forumId)))
      .onFailure(throwable -> message.fail(406, "Can't open Forum with Id %s".formatted(forumId)));
  }

  @Override
  public void handle(final Message<Commands.Open> message, final String aggregateId, final Commands.Open content, final String by, final Headers headers) {
    final var forumId = headers.aggregateId(Forum.attributes::id).orElseThrow();
  }
}
