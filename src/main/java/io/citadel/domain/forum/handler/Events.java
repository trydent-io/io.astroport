package io.citadel.domain.forum.handler;

import io.citadel.domain.forum.Forum;
import io.citadel.kernel.eventstore.data.EventInfo;
import io.vertx.core.json.JsonObject;

import java.util.Optional;

public enum Events {
  Companion;

  public Forum.Event replaced(final Forum.Details details) {return new Replaced(details);}
  public Forum.Event registered(Forum.Details details) {return new Registered(details);}
  public Forum.Event opened() {return new Opened();}
  public Forum.Event closed() {return new Closed();}
  public Forum.Event reopened() {return new Reopened();}
  public Forum.Event archived() {return new Archived();}
  public Forum.Event fromInfo(EventInfo event) {
    return from(event.name(), event.data()).orElseThrow();
  }

  public enum Names {Opened, Closed, Registered, Reopened, Replaced, Archived}

  public Optional<Forum.Event> from(String name, JsonObject json) {
    return switch (name) {
      case "Opened" -> Optional.of(json.mapTo(Opened.class));
      case "Closed" -> Optional.of(json.mapTo(Closed.class));
      case "Registered" -> Optional.of(json.mapTo(Registered.class));
      case "Reopened" -> Optional.of(json.mapTo(Reopened.class));
      case "Replaced" -> Optional.of(json.mapTo(Replaced.class));
      case "Archived" -> Optional.of(json.mapTo(Archived.class));
      default -> Optional.empty();
    };
  }

  public record Registered(Forum.Details details) implements Forum.Event {}
  public record Replaced(Forum.Details details) implements Forum.Event {}
  public record Opened() implements Forum.Event {}
  public record Closed() implements Forum.Event {}
  public record Reopened() implements Forum.Event {}
  public record Archived() implements Forum.Event {}
}
