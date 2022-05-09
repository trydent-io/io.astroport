package io.citadel.domain.forum.handler.command;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.forum.aggregate.Forums;
import io.citadel.domain.forum.handler.Commands;
import io.citadel.kernel.domain.Headers;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public record Archive(Forums forums) implements Forum.Handler<Commands.Archive> {
  @Override
  public Class<Commands.Archive> type() {
    return Commands.Archive.class;
  }

  @Override
  public void handle(Message<JsonObject> message, Forum.ID forumId, Commands.Archive archive, String by, Headers headers) {
    forums
      .forum(forumId)
      .compose(Forum::archive)
      .compose(forum -> forum.submit(by))
      .onSuccess(aggregate -> message.reply("Forum with Id %s has been archived".formatted(forumId)))
      .onFailure(throwable -> message.fail(406, "Can't archive Forum with Id %s".formatted(forumId)));
  }
}
