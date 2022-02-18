package io.citadel.forum.state;

import io.citadel.forum.Forum;
import io.citadel.forum.model.Model;
import io.citadel.kernel.domain.Domain;

public enum States {
  Defaults;

  public Forum initial(Forum.ID identity) {
    return new Identity(identity);
  }

  public Forum opened(Model model, Forum.Event event) {
    return new Opened(model, event);
  }

  public Forum closed(Model model, Domain.Event... events) {
    return new Forum.Aggregate(model, Forum.State.Closed, events);
  }
}
