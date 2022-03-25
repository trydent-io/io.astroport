package io.citadel.domain.forum.entity;

import io.citadel.domain.forum.Forum;

import java.time.LocalDateTime;
import java.util.Optional;

import static io.citadel.domain.forum.Forum.State.Archived;
import static io.citadel.domain.forum.Forum.State.Closed;
import static io.citadel.domain.forum.Forum.State.Open;
import static io.citadel.domain.forum.Forum.State.Registered;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public record Model(Forum.ID id, Forum.Name name, Forum.Description description, LocalDateTime registeredAt, LocalDateTime openedAt, LocalDateTime closedAt, LocalDateTime archivedAt, Forum.State state) implements Forum.Entity {
  public Model(Forum.ID id) { this(id, null, null, null, null, null, null, null); }

  @Override
  public boolean is(final Forum.State state) {
    return nonNull(this.state) && this.state.equals(state);
  }

  @Override
  public Optional<Forum.Entity> edit(final Forum.Name name) {
    return Optional.ofNullable(state)
      .filter(it -> it.is(Registered, Open))
      .map(it -> new Model(id, name, description, registeredAt, openedAt, closedAt, archivedAt, state));
  }

  @Override
  public Optional<Forum.Entity> edit(final Forum.Description description) {
    return Optional.ofNullable(state)
      .filter(it -> it.is(Registered, Open))
      .map(it -> new Model(id, name, description, registeredAt, openedAt, closedAt, archivedAt, state));
  }

  @Override
  public Optional<Forum.Entity> register(final Forum.Name name, final Forum.Description description, final LocalDateTime registeredAt) {
    return Optional.ofNullable(isNull(state) ? new Model(id, name, description, registeredAt, openedAt, closedAt, archivedAt, Registered) : null);
  }

  @Override
  public Optional<Forum.Entity> open(final LocalDateTime openedAt) {
    return Optional.ofNullable(state)
      .filter(it -> it.equals(Registered))
      .map(it -> new Model(id, name, description, registeredAt, openedAt, closedAt, archivedAt, Open));
  }

  @Override
  public Optional<Forum.Entity> close(final LocalDateTime closedAt) {
    return Optional.ofNullable(state)
      .filter(it -> it.equals(Open))
      .map(it -> new Model(id, name, description, registeredAt, openedAt, closedAt, archivedAt, Closed));
  }

  @Override
  public Optional<Forum.Entity> archive(final LocalDateTime archivedAt) {
    return Optional.ofNullable(state)
      .filter(it -> it.equals(Closed))
      .map(it -> new Model(id, name, description, registeredAt, openedAt, closedAt, archivedAt, Archived));
  }
}
