package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.kernel.domain.Domain;

import java.util.stream.Stream;

public sealed interface Aggregate extends Forum<Aggregate>, Domain.Aggregate permits Transaction, Root {
  static Aggregate root(Forum.ID id, long version) {
    return new Root(new Model(id), version, Stream.empty());
  }

  static Aggregate root(Model model, long version) {
    return new Root(model, version, Stream.empty());
  }

  static Aggregate transaction(Lifecycle<Aggregate> lifecycle) { return new Transaction(lifecycle); }
}

