package io.citadel.forum;

import io.citadel.forum.attribute.Attributes;
import io.citadel.forum.command.Close;
import io.citadel.forum.command.Commands;
import io.citadel.forum.command.Open;
import io.citadel.forum.event.Events;
import io.citadel.forum.state.Model;
import io.citadel.forum.state.States;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.media.ArrayIterator;

import java.util.Iterator;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

public interface Forum extends Domain.Aggregate<Forum.Command, Forum.State, Forum> {
  Commands commands = Commands.Defaults;
  Events events = Events.Defaults;
  States states = States.Defaults;
  Attributes attributes = Attributes.Defaults;

  record ID(UUID get) implements Domain.ID<UUID> {
  }

  sealed interface Command extends Domain.Command permits Close, Open {
  }

  interface Event extends Domain.Event {
  }

  enum State implements Domain.State<Forum.State> {Opened, Closed}

  static Forum from(ID identity, Stream<Domain.Event> events) {
    return events
      .map(Domain.Event::asCommand)
      .map(command -> command instanceof Forum.Command fc ? fc : null)
      .reduce(Forum.states.initial(identity), Forum::apply, (f, f2) -> f2)
      .flush();
  }

  final class Aggregate implements Forum {
    private final Model model;
    private final Forum.State state;
    private final Domain.Event[] changes;

    public Aggregate(Model model, State state, Domain.Event... events) {
      this.model = model;
      this.state = state;
      this.changes = events;
    }

    public Aggregate(Model model) {
      this(model, null);
    }

    @Override
    public Forum flush() {
      return new Aggregate(model, state);
    }

    private Forum apply(Open open) {
      return Forum.states.opened(
        model
          .name(open.title())
          .description(open.description())
          .openedAt(open.at())
          .openedBy(open.by()),
        open.asEvents()
      );
    }

    private Forum apply(Close close) {
      return Forum.states.closed(
        model
          .closedAt(close.at())
          .closedBy(close.by()),
        close.asEvents()
      );
    }

    @Override
    public Forum apply(Command command) {
      return switch (command) {
        case Open open -> apply(open);
        case Close close -> apply(close);
      };
    }

    @Override
    public Iterator<Domain.Event> iterator() {
      return new ArrayIterator<>(changes);
    }
  }
}
