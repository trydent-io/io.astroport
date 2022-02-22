package io.citadel.forum.command;

import io.citadel.forum.Forum;

public sealed interface Edit extends Forum.Command {
  record Description(Forum.Description description) implements Edit {
    @Override
    public Forum.Event asEvent() {
      return Forum.events.edited(description);
    }
  }
  record Name(Forum.Name name) implements Edit {
    @Override
    public Forum.Event asEvent() {
      return Forum.events.edited(name);
    }
  }
}
