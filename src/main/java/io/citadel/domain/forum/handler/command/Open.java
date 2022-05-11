package io.citadel.domain.forum.handler.command;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.forum.aggregate.Forums;
import io.citadel.domain.forum.handler.Commands;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.domain.Headers;
import io.citadel.kernel.vertx.Behaviours;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;

public record Open(Forums forums) implements Forum.Handler<Commands.Open> {
  @Override
  public void handle(final Message<Commands.Open> message, final Forum.ID forumId, final Commands.Open open, final String by, final Headers headers) {
    forums
      .forum(forumId)
      .compose(Forum.Aggregate::open)
      .compose(Forum.Aggregate::submit)
      .onSuccess(aggregate -> message.reply("Forum with Id %s has been opened".formatted(forumId)))
      .onFailure(throwable -> message.fail(406, "Can't open Forum with Id %s".formatted(forumId)));
  }

  @Override
  public Behaviours bind(final Behaviours behaviours) {
    return behaviours.be(Commands.Open.class, "forum.open", this);
  }
}
