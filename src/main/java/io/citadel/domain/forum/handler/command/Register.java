package io.citadel.domain.forum.handler.command;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.forum.aggregate.Forums;
import io.citadel.domain.forum.handler.Commands;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.domain.Headers;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public record Register(Forums forums) implements Forum.Handler<Commands.Register> {
  @Override
  public Class<Commands.Register> type() {
    return Commands.Register.class;
  }

  @Override
  public void handle(Message<JsonObject> message, Forum.ID forumId, Commands.Register register, String by, Headers headers) {
    forums
      .forum(forumId)
      .compose(forum -> forum.register(new Forum.Details(register.name(), register.description())))
      .compose(forum -> forum.submit(by))
      .onSuccess(aggregate -> message.reply("Forum with Id %s has been registered".formatted(forumId)))
      .onFailure(throwable -> message.fail(406, "Can't register Forum with Id %s".formatted(forumId)));
  }
}
