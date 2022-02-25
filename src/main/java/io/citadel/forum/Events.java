package io.citadel.forum;

import io.citadel.kernel.domain.Domain;
import io.citadel.member.MemberID;
import io.vertx.core.json.JsonObject;

import java.time.LocalDateTime;
import java.util.Optional;

public enum Events {
  Defaults;

  public Forum.Event edited(final Forum.Description description) {
    return new Edited.Description(description);
  }

  public Forum.Event edited(final Forum.Name name) {
    return new Edited.Name(name);
  }

  public Forum.Event registered(Forum.Name name, Forum.Description description, LocalDateTime at, MemberID by) {
    return new Registered(name, description, at, by);
  }

  public Forum.Event opened(final LocalDateTime at, final MemberID by) {
    return new Opened(at, by);
  }

  public Forum.Event closed(final LocalDateTime at, final MemberID by) {
    return new Closed(at, by);
  }

  public Forum.Event reopened(final LocalDateTime at, final MemberID by) {
    return new Reopened(at, by);
  }

  private enum Names { Opened, Closed }

  public Optional<Domain.Event> from(String name, JsonObject json) {
    try {
      return Optional.of(switch (Names.valueOf(name)) {
        case Opened -> json.mapTo(Opened.class);
        case Closed -> json.mapTo(Closed.class);
      });
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  public record Closed(LocalDateTime at, MemberID by) implements Forum.Event {}

  public record Opened(LocalDateTime at, MemberID by) implements Forum.Event {}

  public record Registered(Forum.Name name, Forum.Description description, LocalDateTime at, MemberID by) implements Forum.Event {}

  public record Reopened(LocalDateTime at, MemberID memberID) implements Forum.Event {}

  public sealed static interface Edited extends Forum.Event {
    record Name(Forum.Name name) implements Edited {}
    record Description(Forum.Description description) implements Edited {}
  }
}
