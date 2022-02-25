package io.citadel.forum.command;

import io.citadel.forum.Forum;

public sealed interface Edit extends Forum.Command {
  record Description(Forum.Description description) implements Edit {}
  record Name(Forum.Name name) implements Edit {}
}
