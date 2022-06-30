package io.citadel.domain.forum.handler.command;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.forum.Forum.Command;
import io.vertx.core.Future;

public record Register() implements Forum.Handler<Command.Register> {

  @Override
  public Future<Void> handle(Forum forum, Command.Register register) {
    return forum.register(register.name(), register.description()).commit();
  }
}
