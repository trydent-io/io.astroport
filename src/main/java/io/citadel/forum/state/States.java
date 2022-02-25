package io.citadel.forum.state;

import io.citadel.forum.Forum;
import io.citadel.forum.model.State;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.func.ThrowableTriFunction;
import io.citadel.kernel.media.Array;

public sealed interface States extends Forum {

  record Initial(Forum.ID id, Domain.Version version) implements States {
    @Override
    public Forum tryApply(final ThrowableTriFunction<Domain.ID<?>, Domain.Version, Domain.Event[], Forum> apply) throws Throwable {
      return null;
    }
  }
  record Registered(Forum.ID id, Domain.Version version, State state, Array<Forum.Event> events) implements States {
    @Override
    public Forum tryApply(final ThrowableTriFunction<Domain.ID<?>, Domain.Version, Domain.Event[], Forum> apply) throws Throwable {
      return null;
    }
  }
  record Open(Forum.ID id, Domain.Version version, State state, Array<Forum.Event> events) implements States {
    @Override
    public Forum tryApply(final ThrowableTriFunction<Domain.ID<?>, Domain.Version, Domain.Event[], Forum> apply) throws Throwable {
      return null;
    }
  }
  record Closed(Forum.ID id, Domain.Version version, State state, Array<Forum.Event> events) implements States {
    @Override
    public Forum tryApply(final ThrowableTriFunction<Domain.ID<?>, Domain.Version, Domain.Event[], Forum> apply) throws Throwable {
      return null;
    }
  }
}
