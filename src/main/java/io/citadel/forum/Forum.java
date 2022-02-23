package io.citadel.forum;

import io.citadel.forum.command.Close;
import io.citadel.forum.command.Commands;
import io.citadel.forum.command.Edit;
import io.citadel.forum.command.Open;
import io.citadel.forum.command.Register;
import io.citadel.forum.command.Reopen;
import io.citadel.forum.event.Closed;
import io.citadel.forum.event.Edited;
import io.citadel.forum.event.Events;
import io.citadel.forum.event.Opened;
import io.citadel.forum.event.Registered;
import io.citadel.forum.event.Reopened;
import io.citadel.forum.model.Attributes;
import io.citadel.forum.model.Model;
import io.citadel.forum.state.ClosedForum;
import io.citadel.forum.state.Initial;
import io.citadel.forum.state.OpenedForum;
import io.citadel.forum.state.Registerable;
import io.citadel.forum.state.RegisteredForum;
import io.citadel.forum.state.States;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.func.ThrowableTriFunction;

import java.util.Map;
import java.util.UUID;

public sealed interface Forum extends Registerable permits ClosedForum, Initial, OpenedForum, RegisteredForum {
  String NAME = "Forum";

  Commands commands = Commands.Defaults;
  Events events = Events.Defaults;
  States states = States.Defaults;
  Attributes attributes = Attributes.Defaults;

  static Forum of(ID identity) {
    return new Aggregate(Model.with(identity), Domain.Version.zero(), )
  }

  static Forum from(ID identity, Map<Domain.Version, Forum.Event> events) {
    return events
      .map(Forum.Event::asCommand)
      .reduce(Forum.states.initial(identity), Forum::apply, (f, f2) -> f2)
      .flush();
  }

  sealed interface Command extends Domain.Command<Forum.Event>
    permits Close, Edit, Open, Register, Reopen {}

  sealed interface Event extends Domain.Event<Forum.Command>
    permits Closed, Edited, Opened, Registered, Reopened {}

  record ID(UUID value) implements Domain.ID<UUID> {}
  record Name(String value) implements Domain.Attribute<String> {}
  record Description(String value) implements Domain.Attribute<String> {}
}

final class Aggregate implements Forum {
  private final Model model;
  private final Domain.Version version;
  private final Event[] events;

  Aggregate(final Model model, final Domain.Version version, final Event... events) {
    this.model = model;
    this.version = version;
    this.events = events;
  }

  @Override
  public Forum tryApply(final ThrowableTriFunction<Domain.ID<?>, Domain.Version, Domain.Event[], Forum> apply) {
    return apply.tryApply();
  }
}
