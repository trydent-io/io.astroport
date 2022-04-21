package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.kernel.domain.Domain;

public sealed interface Aggregate extends Forum<Aggregate>, Domain.Aggregate permits Root {
  static Aggregate root(Model model, long version) {
    return new Root(model, version, new Transaction(new Lifecycle(null)));
  }
}

