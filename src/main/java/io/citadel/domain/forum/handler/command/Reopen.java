package io.citadel.domain.forum.handler.command;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.forum.aggregate.Forums;
import io.citadel.domain.forum.handler.Commands;
import io.citadel.kernel.domain.Headers;
import io.citadel.kernel.vertx.Behaviours;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public record Reopen(Forums forums) implements Forum.Handler<Commands.Reopen> {
  @Override
  public void handle(final Message<Commands.Reopen> message, final Forum.ID forumId, final Commands.Reopen reopen, final String by, final Headers headers) {
    forums
      .forum(forumId)
      .compose(Forum::reopen)
      .compose(forum -> forum.submit(by))
      .onSuccess(aggregate -> message.reply("Forum with Id %s has been reopened".formatted(forumId)))
      .onFailure(throwable -> message.fail(406, "Can't reopen Forum with Id %s".formatted(forumId)));
  }

  @Override
  public Behaviours bind(final Behaviours behaviours) {
    return behaviours.be(Commands.Reopen.class, "forum.reopen", this);
  }
}
