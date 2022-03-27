package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.forum.event.Events;

import java.time.LocalDateTime;
import java.util.Optional;

import static io.citadel.domain.forum.Forum.State.Archived;
import static io.citadel.domain.forum.Forum.State.Closed;
import static io.citadel.domain.forum.Forum.State.Open;
import static io.citadel.domain.forum.Forum.State.Registered;
import static java.util.Objects.isNull;

public final class Model implements Forum.Entity {
  private final Forum.ID id;
  private final Forum.Name name;
  private final Forum.Description description;
  private final LocalDateTime registeredAt;
  private final LocalDateTime openedAt;
  private final LocalDateTime closedAt;
  private final LocalDateTime archivedAt;
  private final Forum.State state;

  public Model(Forum.ID id, Forum.Name name, Forum.Description description, LocalDateTime registeredAt, LocalDateTime openedAt, LocalDateTime closedAt, LocalDateTime archivedAt, Forum.State state) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.registeredAt = registeredAt;
    this.openedAt = openedAt;
    this.closedAt = closedAt;
    this.archivedAt = archivedAt;
    this.state = state;
  }

  public Model(Forum.ID id) {this(id, null, null, null, null, null, null, null);}

  @Override
  public Forum.Entity get() {
    return this;
  }

  @Override
  public Optional<Forum.Entity> apply(final Forum.Event event) {
    return Optional.ofNullable(
      switch (event) {
        case Events.Registered registered && isNull(state) -> new Model(id, registered.name(), registered.description(), registeredAt, openedAt, closedAt, archivedAt, Registered);
        case Events.Edited.Name edited && state.is(Registered, Open) -> new Model(id, edited.name(), description, registeredAt, openedAt, closedAt, archivedAt, state);
        case Events.Edited.Description edited && state.is(Registered, Open) -> new Model(id, name, edited.description(), registeredAt, openedAt, closedAt, archivedAt, state);
        case Events.Closed closed && state.is(Open) -> new Model(id, name, description, registeredAt, openedAt, closedAt, archivedAt, Closed);
        case Events.Reopened reopened && state.is(Closed) -> new Model(id, name, description, registeredAt, openedAt, closedAt, archivedAt, Open);
        case Events.Archived archived && state.is(Closed) -> new Model(id, name, description, registeredAt, openedAt, closedAt, archivedAt, Archived);
        default -> null;
      }
    );
  }
}
