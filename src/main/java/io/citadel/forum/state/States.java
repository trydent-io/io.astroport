package io.citadel.forum.state;

import io.citadel.forum.Forum;
import io.citadel.forum.model.Model;

public enum States {
  Defaults;

  public Forum initial(Forum.ID identity) {
    return new Initial(identity);
  }

  public Forum registered(Model model, Forum.Event event) {
    return new RegisteredForum(model, event);
  }

  public Forum opened(Model model, Forum.Event... event) {
    return new OpenedForum(model, event);
  }

  public Forum closed(Model model, Forum.Event... events) {
    return new ClosedForum(model, events);
  }
}
