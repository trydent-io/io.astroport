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
import io.citadel.forum.state.Closeable;
import io.citadel.forum.state.Editable;
import io.citadel.forum.state.Openable;
import io.citadel.forum.state.Registerable;
import io.citadel.forum.state.States;
import io.citadel.kernel.domain.Domain;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public sealed interface Forum extends Registerable, Openable, Editable, Closeable permits States {
  Commands commands = Commands.Defaults;
  Events events = Events.Defaults;
  Attributes attributes = Attributes.Defaults;

  static Forum of(ID identity) {
    return new States.Initial(identity, Domain.Version.zero());
  }

  static Forum from(ID identity, Domain.Version version, Event... events) {
    return Stream.of(events).reduce(
        Forum.of(identity, version),
        (forum, event) -> switch (event) {
          case Registered registered -> forum.register(registered.name(), registered.description(), registered.at(), registered.by());
          case Opened opened -> forum.open(opened.at(), opened.by());
          case Closed closed -> forum.close(closed.at(), closed.by());
          case Edited.Name edit -> forum.edit(edit.name());
          case Edited.Description edit -> forum.edit(edit.description());
          case Reopened reopened -> forum.open();
        },
        (f, f2) -> f2)
      .flush();
  }

  static Forum of(ID identity, Domain.Version version) {
    return new States.Initial(identity, version);
  }

  sealed interface Command extends Domain.Command<Forum.Event>
    permits Close, Edit, Open, Register, Reopen {}

  sealed interface Event extends Domain.Event<Forum.Command>
    permits Closed, Edited, Opened, Registered, Reopened {}

  record ID(UUID value) implements Domain.ID<UUID> {}

  record Name(String value) implements Domain.Attribute<String> {}

  record Description(String value) implements Domain.Attribute<String> {}
}

