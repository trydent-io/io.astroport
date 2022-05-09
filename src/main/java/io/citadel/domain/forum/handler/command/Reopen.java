package io.citadel.domain.forum.handler.command;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.forum.aggregate.Forums;
import io.citadel.domain.forum.handler.Commands;
import io.citadel.kernel.domain.Headers;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public record Reopen(Forums forums) implements Forum.Handler<Commands.Reopen> {
  @Override
  public Class<Commands.Reopen> type() {
    return Commands.Reopen.class;
  }

  @Override
  public void handle(Message<JsonObject> message, Forum.ID forumId, Commands.Reopen reopen, String by, Headers headers) {
    forums
      .forum(forumId)
      .compose(Forum::reopen)
      .compose(forum -> forum.submit(by))
      .onSuccess(aggregate -> message.reply("Forum with Id %s has been reopened".formatted(forumId)))
      .onFailure(throwable -> message.fail(406, "Can't reopen Forum with Id %s".formatted(forumId)));
  }
}
