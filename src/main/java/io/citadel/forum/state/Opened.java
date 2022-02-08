package io.citadel.forum.state;

import io.citadel.domain.message.Command;
import io.citadel.forum.Forum;

public final class Opened implements Forum {
  @Override
  public Forum apply(final Command command) {
    return null;
  }
}
