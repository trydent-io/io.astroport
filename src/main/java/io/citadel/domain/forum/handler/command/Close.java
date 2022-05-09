package io.citadel.domain.forum.handler.command;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.forum.aggregate.Forums;
import io.citadel.domain.forum.handler.Commands;
import io.citadel.kernel.domain.Headers;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public record Close(Forums forums) implements Forum.Handler<Commands.Close> {
  @Override
  public Class<Commands.Close> type() {
    return Commands.Close.class;
  }

  @Override
  public void handle(Message<JsonObject> message, Forum.ID forumId, Commands.Close close, String by, Headers headers) {
    forums
      .forum(forumId)
      .compose(Forum::close)
      .compose(forum -> forum.submit(by))
      .onSuccess(aggregate -> message.reply("Forum with Id %s has been closed".formatted(forumId)))
      .onFailure(throwable -> message.fail(406, "Can't close Forum with Id %s".formatted(forumId)));
  }
}
