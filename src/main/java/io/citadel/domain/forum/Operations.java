package io.citadel.domain.forum;

import java.time.LocalDateTime;
import java.util.Optional;

public sealed interface Operations<T> permits Forum, Forum.Entity {
  Optional<T> edit(Forum.Name name);

  Optional<T> edit(Forum.Description description);

  Optional<T> register(Forum.Name name, Forum.Description description, LocalDateTime registeredAt);

  Optional<T> open(LocalDateTime openedAt);

  Optional<T> close(LocalDateTime closedAt);

  Optional<T> archive(LocalDateTime archivedAt);

}
