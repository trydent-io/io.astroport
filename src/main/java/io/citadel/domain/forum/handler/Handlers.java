package io.citadel.domain.forum.handler;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.forum.handler.command.Archive;
import io.citadel.domain.forum.handler.command.Close;
import io.citadel.domain.forum.handler.command.Open;
import io.citadel.domain.forum.handler.command.Register;
import io.citadel.domain.forum.handler.command.Reopen;
import io.citadel.domain.forum.handler.command.Replace;
import io.citadel.kernel.eventstore.EventStore;
import io.citadel.kernel.vertx.Switch;

public enum Handlers {
  Companion;

  public Switch bind(Switch switcher, EventStore eventStore) {
    final var forums = Forum.defaults.forums(eventStore);
    return switcher
      .bind("forum.register", new Register(forums))
      .bind("forum.open", new Open(forums))
      .bind("forum.replace", new Replace(forums))
      .bind("forum.close", new Close(forums))
      .bind("forum.archive", new Archive(forums))
      .bind("forum.reopen", new Reopen(forums));
  }
}

