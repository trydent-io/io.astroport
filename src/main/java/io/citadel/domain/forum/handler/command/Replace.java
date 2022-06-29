package io.citadel.domain.forum.handler.command;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.forum.aggregate.Forums;
import io.citadel.domain.forum.handler.Commands;
import io.citadel.kernel.domain.Headers;
import io.vertx.core.eventbus.Message;

public record Replace(Forums forums) implements Forum.Handler<Commands.Replace> {
  @Override
  public void handle(final Message<Commands.Replace> message, final Forum.ID forumId, final Commands.Replace replace, final String by, final Headers headers) {
    forums
      .forum(forumId)
      .compose(forum -> forum.replace(new Forum.Details(replace.name(), replace.description())))
      .compose(forum -> forum.submit(by))
      .onSuccess(aggregate -> message.reply("Forum with Id %s has been replaced".formatted(forumId)))
      .onFailure(throwable -> message.fail(406, "Can't replace Forum with Id %s".formatted(forumId)));
  }

  @Override
  public Behaviours bind(final Behaviours behaviours) {
    return behaviours.be(Commands.Replace.class, "forum.replace", this);
  }
}
