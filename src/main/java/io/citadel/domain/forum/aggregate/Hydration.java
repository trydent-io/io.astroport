package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.forum.handler.Events;
import io.citadel.kernel.domain.eventstore.data.EventInfo;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Stream;

final class Hydration implements Snapshot {
  private Model model;

  Hydration(Model model) {this.model = model;}

  @Override
  public Aggregate aggregate(final String id, final long version, final Stream<EventInfo> events) {
    return Optional.ofNullable(events)
      .map(stream -> stream.map(Forum.events::fromInfo))
      .map(stream -> stream.reduce(identity(id), applyEvent(), (f, __) -> f))
      .map(snapshot -> snapshot instanceof Hydration hydration ? hydration.model : null)
      .map(model -> Forum.defaults.aggregate(model, version))
      .orElseThrow();
  }

  private BiFunction<Snapshot, Event, Snapshot> applyEvent() {
    return (forum, event) -> switch (event) {
      case Events.Registered registered -> forum.register(registered.name(), registered.description());
      case Events.Opened opened -> forum.open();
      case Events.Closed closed -> forum.close();
      case Events.Altered altered -> forum.change(altered.name(), altered.description());
      case Events.Reopened reopened -> forum.reopen();
      case Events.Archived archived -> forum.archive();
    };
  }

  private Snapshot identity(String id) {
    this.model = Forum.defaults.model(id);
    return this;
  }

  @Override
  public Snapshot register(final Name name, final Description description) {
    this.model = new Model(model.id(), new Details(name, description));
    return this;
  }

  @Override
  public Snapshot change(final Name name, final Description description) {
    this.model = new Model(model.id(), new Details(name, description));
    return this;
  }

  @Override
  public Snapshot open() {
    return this;
  }

  @Override
  public Snapshot close() {
    return this;
  }

  @Override
  public Snapshot archive() {
    return this;
  }

  @Override
  public Snapshot reopen() {
    return this;
  }
}

final class Timepoint extends Span<Snapshot> implements Snapshot {
  Timepoint(Lifecycle<Snapshot> lifecycle) {
    super(lifecycle, Timepoint::new);
  }

  @Override
  public Aggregate aggregate(final String id, final long version, final Stream<EventInfo> events) throws Throwable {
    return service.eventually(snapshot -> snapshot.aggregate(id, version, events));
  }
}
