package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.forum.handler.Events;
import io.citadel.kernel.domain.Domain;
import io.vertx.core.json.JsonObject;

import static io.citadel.domain.forum.handler.Events.Names.valueOf;

public record Snapshot(Domain.Archetype<Forum.Model> archetype, Domain.Timeline<Forum.State, Forum.Event, Forum.Model> timeline, long version) implements Domain.Snapshot<Forum.Transaction> {
  Snapshot(Domain.Archetype<Forum.Model> archetype) {this(archetype, null, 0);}

  @Override
  public Domain.Snapshot<Forum.Transaction> archetype(String aggregateId, long aggregateVersion) {
    return new Snapshot(archetype, Domain.Timeline.pastline(Forum.State.Registered, archetype.generate(aggregateId)), aggregateVersion);
  }

  @Override
  public Domain.Snapshot<Forum.Transaction> hydrate(String eventName, JsonObject eventData) {
    return new Snapshot(switch (valueOf(eventName)) {
        case Opened -> stage(eventData.mapTo(Events.Opened.class));
        case Closed -> stage(eventData.mapTo(Events.Closed.class));
        case Registered -> stage(eventData.mapTo(Events.Registered.class));
        case Reopened -> stage(eventData.mapTo(Events.Reopened.class));
        case Replaced -> stage(eventData.mapTo(Events.Replaced.class));
        case Archived -> stage(eventData.mapTo(Events.Archived.class));
      }, version);
  }

  private Domain.Timeline<Forum.State, Forum.Event, Forum.Model> stage(final Forum.Event event) {
    return switch (event) {
      case Events.Registered it -> timeline.point(it, model -> register(model, it.details()));
      case Events.Opened it -> timeline.point(it, model -> model);
      case Events.Replaced it -> timeline.point(it, model -> register(model, it.details()));
      case Events.Closed it -> timeline.point(it, model -> model);
      case Events.Archived it -> timeline.point(it, model -> model);
      case Events.Reopened it -> timeline.point(it, model -> model);
    };
  }

  private Forum.Model register(final Forum.Model model, final Forum.Details it) {
    return new Forum.Model(model.id(), it);
  }

  @Override
  public Aggregate transaction(final EventStore eventStore) {
    return Forum.defaults.aggregate(model, version, timeline, Domain.defaults.transaction(eventStore));
  }
}
