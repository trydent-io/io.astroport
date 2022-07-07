package io.citadel.domain.forum.handler.command;

import io.citadel.domain.forum.Forum;
import io.citadel.kernel.domain.Lookup;
import io.citadel.kernel.domain.Headers;
import io.citadel.kernel.vertx.Task;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;

public final class Async<C extends Record & Forum.Command> implements Task.Handler<C> {
  private final MessageConsumer<C> local;
  private final Lookup<Forum> root;
  private final Forum.Handler<C> handler;

  public Async(MessageConsumer<C> local, Lookup<Forum> root, Forum.Handler<C> handler) {
    this.local = local;
    this.root = root;
    this.handler = handler;
    local.handler(this);
  }

  @Override
  public void handle(Headers headers, Message<C> message) {
    root.aggregate(headers.id()).onSuccess(forum -> handler.handle(forum, message.body()));
  }
}
