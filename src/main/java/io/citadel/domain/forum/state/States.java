package io.citadel.domain.forum.state;

import io.citadel.domain.forum.Forum;
import io.citadel.domain.forum.Events;
import io.citadel.domain.forum.model.State;
import io.citadel.shared.domain.Domain;
import io.citadel.shared.func.ThrowableTriFunction;
import io.citadel.shared.media.Array;

import java.util.stream.Stream;

public enum States {
  Defaults;

  public Forum of(Forum.ID identity) {
    return new States.Initial(identity, Domain.Version.zero());
  }

  public Forum from(Forum.ID identity, Domain.Version version, Forum.Event... events) {
    return Stream.of(events).reduce(
        Forum.states.of(identity, version),
        (forum, event) -> switch (event) {
          case Events.Registered registered -> forum.register(registered.name(), registered.description(), registered.at(), registered.by());
          case Events.Opened opened -> forum.open(opened.at(), opened.by());
          case Events.Closed closed -> forum.close(closed.at(), closed.by());
          case Events.Edited.Name edit -> forum.edit(edit.name());
          case Events.Edited.Description edit -> forum.edit(edit.description());
          case Events.Reopened reopened -> forum.reopen(reopened.at(), reopened.memberID());
        },
        (f, f2) -> f2)
      .flush();
  }

  public Forum of(Forum.ID identity, Domain.Version version) {
    return new States.Initial(identity, version);
  }


  public record Initial(Forum.ID id, Domain.Version version) implements Forum {
    @Override
    public Forum tryApply(final ThrowableTriFunction<Domain.ID<?>, Domain.Version, Domain.Event[], Forum> apply) {
      return null;
    }
  }
  public record Registered(Forum.ID id, Domain.Version version, State state, Array<Forum.Event> events) implements Forum {
    @Override
    public Forum tryApply(final ThrowableTriFunction<Domain.ID<?>, Domain.Version, Domain.Event[], Forum> apply) {
      return null;
    }
  }
  public record Open(Forum.ID id, Domain.Version version, State state, Array<Forum.Event> events) implements Forum {
    @Override
    public Forum tryApply(final ThrowableTriFunction<Domain.ID<?>, Domain.Version, Domain.Event[], Forum> apply) {
      return null;
    }
  }
  public record Closed(Forum.ID id, Domain.Version version, State state, Array<Forum.Event> events) implements Forum {
    @Override
    public Forum tryApply(final ThrowableTriFunction<Domain.ID<?>, Domain.Version, Domain.Event[], Forum> apply) {
      return null;
    }
  }
}
