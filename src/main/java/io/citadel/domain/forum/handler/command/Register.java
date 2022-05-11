package io.citadel.domain.forum.handler.command;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.forum.aggregate.Forums;
import io.citadel.domain.forum.handler.Commands;
import io.citadel.kernel.domain.Headers;
import io.citadel.kernel.vertx.Behaviours;
import io.vertx.core.eventbus.Message;

public record Register(Forums forums) implements Forum.Handler<Commands.Register> {
  @Override
  public void handle(final Message<Commands.Register> message, final Forum.ID forumId, final Commands.Register register, final String by, final Headers headers) {
    forums
      .forum(forumId)
      .compose(forum -> forum.register(new Forum.Details(register.name(), register.description())))
      .compose(forum -> forum.submit(by))
      .onSuccess(aggregate -> message.reply("Forum with Id %s has been registered".formatted(forumId)))
      .onFailure(throwable -> message.fail(406, "Can't register Forum with Id %s".formatted(forumId)));
  }

  @Override
  public Behaviours bind(final Behaviours behaviours) {
    return behaviours.be(Commands.Register.class, "forum.register", this);
  }
}
