package io.citadel.forum.state;

import io.citadel.forum.Forum;
import io.citadel.kernel.domain.Domain;

public enum States {
  Defaults;

  public Forum initial(Forum.ID identity) {
    return new Forum.Aggregate(Model.of(identity));
  }

  public Forum opened(Model model, Domain.Event... events) {
    return new Forum.Aggregate(model, Forum.State.Opened, events);
  }

  public Forum closed(Model model, Domain.Event... events) {
    return new Forum.Aggregate(model, Forum.State.Closed, events);
  }
}
