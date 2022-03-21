package io.citadel.domain.forum.event;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.member.Member;
import io.vertx.core.json.JsonObject;

import java.util.Optional;

import io.citadel.eventstore.data.EventInfo;

public enum Events {
  Defaults;

  public Forum.Event edited(final Forum.Description description) {
    return new Edited.Description(description);
  }

  public Forum.Event edited(final Forum.Name name) {
    return new Edited.Name(name);
  }

  public Forum.Event registered(Forum.Name name, Forum.Description description, Member.ID by) {
    return new Registered(name, description, by);
  }

  public Forum.Event opened(Member.ID by) {
    return new Opened(by);
  }

  public Forum.Event closed(Member.ID by) {
    return new Closed(by);
  }

  public Forum.Event reopened(Member.ID by) {
    return new Reopened(by);
  }

  public Forum.Event fromEventInfo(EventInfo event) {
    return from(event.name(), event.data()).orElseThrow();
  }

  private enum Names { Opened, Closed, Registered, Reopened }

  public Optional<Forum.Event> from(String name, JsonObject json) {
    try {
      return Optional.of(switch (Names.valueOf(name)) {
        case Opened -> json.mapTo(Opened.class);
        case Closed -> json.mapTo(Closed.class);
        case Registered -> json.mapTo(Registered.class);
        case Reopened -> json.mapTo(Reopened.class);
      });
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  public record Closed(Member.ID by) implements Forum.Event {}

  public record Opened(Member.ID by) implements Forum.Event {}

  public record Registered(Forum.Name name, Forum.Description description, Member.ID by) implements Forum.Event {}

  public record Reopened(Member.ID by) implements Forum.Event {}

  public sealed interface Edited extends Forum.Event {
    record Name(Forum.Name name) implements Edited {}
    record Description(Forum.Description description) implements Edited {}
  }
}
