package io.citadel.domain.forum.handler.command;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.forum.aggregate.Forums;
import io.citadel.domain.forum.handler.Commands;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.domain.Headers;
import io.vertx.core.eventbus.Message;

public record Register(Forums forums) implements Domain.Handler<Commands.Register> {
  @Override
  public void handle(final Message<Commands.Register> message, final String aggregateId, final Commands.Register register, final String by, final Headers headers) {
    final var forumId = Forum.attributes.id(aggregateId);
/*
    forums
      .forum(forumId)
      .compose(aggregate -> aggregate.register(new Forum.Details(register.name(), register.description())))
      .compose(aggregate -> aggregate.transform(model -> ))
      .compose(aggregate -> aggregate.submit(by))
      .onSuccess(aggregate -> message.reply("Forum with Id %s has been registered".formatted(forumId)))
      .onFailure(throwable -> message.fail(406, "Can't register Forum with Id %s".formatted(forumId)));*/
  }
}
