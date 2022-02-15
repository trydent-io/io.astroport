package io.citadel.forum.command;

import java.time.LocalDateTime;
import java.util.UUID;

public enum Commands {
  Defaults;

  public Open open(String title, final String description, final UUID by, final LocalDateTime at) { return new Open(title, description, by, at); }
}
