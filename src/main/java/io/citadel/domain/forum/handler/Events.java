package io.citadel.domain.forum.handler;

import io.citadel.domain.forum.Forum;
import io.vertx.core.json.JsonObject;

public enum Events {
  Companion;

  public Forum.Event replaced(final Forum.Details details) {return new Replaced(details);}
  public Forum.Event registered(Forum.Details details) {return new Registered(details);}
  public Forum.Event opened() {return new Opened();}
  public Forum.Event closed() {return new Closed();}
  public Forum.Event reopened() {return new Reopened();}
  public Forum.Event archived() {return new Archived();}

  private enum Names {Opened, Closed, Registered, Reopened, Replaced, Archived}

  public Forum.Event from(String name, JsonObject json) {
    return switch (Names.valueOf(name)) {
      case Opened -> json.mapTo(Opened.class);
      case Closed -> json.mapTo(Closed.class);
      case Registered -> json.mapTo(Registered.class);
      case Reopened -> json.mapTo(Reopened.class);
      case Replaced -> json.mapTo(Replaced.class);
      case Archived -> json.mapTo(Archived.class);
    };
  }

  public record Registered(Forum.Details details) implements Forum.Event {}
  public record Replaced(Forum.Details details) implements Forum.Event {}
  public record Opened() implements Forum.Event {}
  public record Closed() implements Forum.Event {}
  public record Reopened() implements Forum.Event {}
  public record Archived() implements Forum.Event {}
}
