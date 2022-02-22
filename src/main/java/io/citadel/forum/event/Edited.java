package io.citadel.forum.event;

import io.citadel.forum.Forum;

public sealed interface Edited extends Forum.Event {
  record Name(Forum.Name name) implements Edited {
    @Override
    public Forum.Command asCommand() {
      return Forum.commands.edit(name);
    }
  }
  record Description(Forum.Description description) implements Edited {
    @Override
    public Forum.Command asCommand() {
      return Forum.commands.edit(description);
    }
  }
}
