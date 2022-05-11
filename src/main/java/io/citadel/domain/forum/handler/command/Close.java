package io.citadel.domain.forum.handler.command;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.forum.aggregate.Forums;
import io.citadel.domain.forum.handler.Commands;
import io.citadel.kernel.domain.Headers;
import io.citadel.kernel.vertx.Behaviours;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public record Close(Forums forums) implements Forum.Handler<Commands.Close> {
  @Override
  public void handle(final Message<Commands.Close> message, final Forum.ID forumId, final Commands.Close close, final String by, final Headers headers) {
    forums
      .forum(forumId)
      .compose(Forum::close)
      .compose(forum -> forum.submit(by))
      .onSuccess(aggregate -> message.reply("Forum with Id %s has been closed".formatted(forumId)))
      .onFailure(throwable -> message.fail(406, "Can't close Forum with Id %s".formatted(forumId)));
  }

  @Override
  public Behaviours bind(final Behaviours behaviours) {
    return behaviours.be(Commands.Close.class, "forum.close", this);
  }
}
