package io.citadel.domain.forum.state;

import io.citadel.domain.forum.Forum;
import io.citadel.kernel.domain.Domain;
import io.vertx.core.Future;

public enum States {
  Defaults;

  public Forum of(Forum.ID identity) {
    return identity(identity, 0);
  }

  public Forum identity(Forum.ID identity, long version) {
    return new Aggregate(Domain.Model.identity(identity, version, Forum.State.Initial));
  }

  public record Aggregate(Forum.ID id, Forum.Name name, Forum.Description description) implements Forum, Forum.Registered, Forum.Open, Forum.Closed, Forum.Archived {
    public Aggregate(ID id) {
      this(id, null, null);
    }

    @Override
    public Open open() {
      return new Aggregate(id, name, description);
    }

    @Override
    public Closed close() {
      return new Aggregate(id, name, description);
    }

    @Override
    public Open reopen() {
      return new Aggregate(id, name, description);
    }

    @Override
    public Archived archive() {
      return new Aggregate(id, name, description);
    }

    @Override
    public Future<Forum> commit(Domain.Transaction<Forum> transaction) {
      return null;
    }

    @Override
    public Registered register(Name name, Description description) {
      return new Aggregate(id, name, description);
    }
  }
}
