package io.citadel.domain.forum.handler.command;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.forum.aggregate.Forums;
import io.citadel.domain.forum.handler.Commands;
import io.citadel.kernel.domain.Headers;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public record Replace(Forums forums) implements Forum.Handler<Commands.Replace> {
  @Override
  public Class<Commands.Replace> type() {
    return Commands.Replace.class;
  }

  @Override
  public void handle(Message<JsonObject> message, Forum.ID forumId, Commands.Replace replace, String by, Headers headers) {
    forums
      .forum(forumId)
      .compose(forum -> forum.replace(new Forum.Details(replace.name(), replace.description())))
      .compose(forum -> forum.submit(by))
      .onSuccess(aggregate -> message.reply("Forum with Id %s has been replaced".formatted(forumId)))
      .onFailure(throwable -> message.fail(406, "Can't replace Forum with Id %s".formatted(forumId)));
  }
}
