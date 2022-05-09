package io.citadel.domain.forum.handler.command;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.forum.aggregate.Forums;
import io.citadel.domain.forum.handler.Commands;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.domain.Headers;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;

public record Open(Forums forums) implements Forum.Handler<Commands.Open> {
  @Override
  public Class<Commands.Open> type() {
    return Commands.Open.class;
  }
  @Override
  public EventBus bind(EventBus eventBus) {
    final var consumer = eventBus.localConsumer("forum.open", this);
    return eventBus;
  }

  @Override
  public void handle(Message<JsonObject> message, Forum.ID forumId, Commands.Open open, String by, Headers headers) {
    forums
      .forum(forumId)
      .compose(Forum.Aggregate::open)
      .compose(Forum.Aggregate::submit)
      .onSuccess(aggregate -> message.reply("Forum with Id %s has been opened".formatted(forumId)))
      .onFailure(throwable -> message.fail(406, "Can't open Forum with Id %s".formatted(forumId)));
  }
}
