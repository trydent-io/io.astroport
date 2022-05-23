package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.forum.handler.Events;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.eventstore.EventStore;
import io.vertx.core.json.JsonObject;

import java.util.Optional;

import static io.citadel.domain.forum.handler.Events.Names.valueOf;

public record Snapshot(Domain.Timeline<Forum.Event> timeline, Forum.Model model, long version) implements Domain.Timeline<Forum.Event>, Domain.Snapshot<Forum.Aggregate> {
  Snapshot(Stage stage, Forum.Model model) {this(stage, model, -1);}

  @Override
  public Domain.Snapshot<Forum.Aggregate> archetype(String aggregateId, long aggregateVersion) {
    return new Snapshot(timeline, Forum.defaults.model(aggregateId), version);
  }

  @Override
  public Domain.Snapshot<Forum.Aggregate> hydrate(String eventName, JsonObject eventData) {
    return (switch (valueOf(eventName)) {
        case Opened -> stage(eventData.mapTo(Events.Opened.class));
        case Closed -> stage(eventData.mapTo(Events.Closed.class));
        case Registered -> stage(eventData.mapTo(Events.Registered.class));
        case Reopened -> stage(eventData.mapTo(Events.Reopened.class));
        case Replaced -> stage(eventData.mapTo(Events.Replaced.class));
        case Archived -> stage(eventData.mapTo(Events.Archived.class));
      }).orElseThrow();
  }

  @SuppressWarnings("DuplicateBranchesInSwitch")
  @Override
  public Optional<Domain.Timeline<Forum.Event>> stage(final Forum.Event event) {
    return timeline.stage(event).map(lifecycle ->
      switch (event) {
        case Events.Registered e -> new Snapshot(lifecycle, new Forum.Model(model.id(), e.details()), version);
        case Events.Opened e -> new Snapshot(lifecycle, model, version);
        case Events.Replaced e -> new Snapshot(lifecycle, new Forum.Model(model.id(), e.details()), version);
        case Events.Closed e -> new Snapshot(lifecycle, model, version);
        case Events.Archived e -> new Snapshot(lifecycle, model, version);
        case Events.Reopened e -> new Snapshot(lifecycle, model, version);
      });
    }

  @Override
  public Aggregate transaction(final EventStore eventStore) {
    return Forum.defaults.aggregate(model, version, timeline, Domain.defaults.transaction(eventStore));
  }
}
