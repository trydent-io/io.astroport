package io.citadel.forum.state;

import io.citadel.forum.Forum;

public enum States {
  Defaults;

  public Forum initial() { return Default.Initial; }

  public Forum opened() {
    return null;
  }
}
