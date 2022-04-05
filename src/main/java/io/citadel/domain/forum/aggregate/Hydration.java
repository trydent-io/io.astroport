package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.forum.event.Events;
import io.citadel.eventstore.data.EventInfo;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Stream;

record Hydration(Model model) implements Snapshot {
  @Override
  public Aggregate aggregate(final long version, final Stream<EventInfo> events) {
    return Optional.ofNullable(events)
      .map(stream -> stream.map(Forum.event::fromInfo))
      .map(stream -> stream.reduce(this, applyEvent(), (f, __) -> f))
      .map(snapshot -> snapshot instanceof Hydration hydration ? hydration.model : null)
      .map(model -> Forum.defaults.aggregate(model, version))
      .orElseThrow();
  }

  private BiFunction<Snapshot, Event, Snapshot> applyEvent() {
    return (forum, event) -> switch (event) {
      case Events.Registered registered -> forum.register(registered.name(), registered.description());
      case Events.Opened opened -> forum.open();
      case Events.Closed closed -> forum.close();
      case Events.Changed changed -> forum.change(changed.name(), changed.description());
      case Events.Reopened reopened -> forum.reopen();
      case Events.Archived archived -> forum.archive();
    };
  }

  @Override
  public Snapshot register(final Name name, final Description description) {
    return new Hydration(new Model(model.id(), new Details(name, description)));
  }

  @Override
  public Snapshot change(final Name name, final Description description) {
    return new Hydration(new Model(model.id(), new Details(name, description)));
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

final class Timepoint extends Lifespan<Snapshot> implements Snapshot {
  Timepoint(Lifecycle<Snapshot> lifecycle) {
    super(lifecycle, Timepoint::new);
  }

  @Override
  public Aggregate aggregate(final long version, final Stream<EventInfo> events) throws Throwable {
    return service.eventually(snapshot -> snapshot.aggregate(version, events));
  }
}
