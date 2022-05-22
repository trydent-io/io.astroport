package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.forum.handler.Events;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.eventstore.EventStore;
import io.vertx.core.json.JsonObject;

import java.util.Optional;

import static io.citadel.domain.forum.handler.Events.Names.valueOf;

public record Snapshot(Forum lifecycle, Model model, long version) implements Forum, Domain.Snapshot<Forum.Aggregate> {
  Snapshot(Stage stage, Model model) {this(stage, model, -1);}

  @Override
  public Forum.Aggregate apply(String id, long version, String eventName, JsonObject eventData) {
    return switch (valueOf(eventName)) {
        case Opened -> assembly(eventData.mapTo(Events.Opened.class));
        case Closed -> assembly(eventData.mapTo(Events.Closed.class));
        case Registered -> assembly(eventData.mapTo(Events.Registered.class));
        case Reopened -> assembly(eventData.mapTo(Events.Reopened.class));
        case Replaced -> assembly(eventData.mapTo(Events.Replaced.class));
        case Archived -> assembly(eventData.mapTo(Events.Archived.class));
      };
  }

  @SuppressWarnings("DuplicateBranchesInSwitch")
  @Override
  public Optional<Forum> assembly(final Event event) {
    return lifecycle.assembly(event).map(forum ->
      switch (event) {
        case Events.Registered e -> new Snapshot(forum, new Model(model.id(), e.details()), version);
        case Events.Opened e -> new Snapshot(forum, model, version);
        case Events.Replaced e -> new Snapshot(forum, new Model(model.id(), e.details()), version);
        case Events.Closed e -> new Snapshot(forum, model, version);
        case Events.Archived e -> new Snapshot(forum, model, version);
        case Events.Reopened e -> new Snapshot(forum, model, version);
      });
    }

  @Override
  public Aggregate aggregate(final EventStore eventStore) {
    return Forum.defaults.aggregate(model, version, lifecycle, Domain.defaults.transaction(eventStore));
  }
}
