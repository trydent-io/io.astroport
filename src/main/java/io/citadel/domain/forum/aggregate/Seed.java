package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.kernel.domain.Domain;

import java.util.stream.Stream;

public sealed interface Seed<T> extends Forum<Seed<T>>, Domain.Seed<T> permits Snap, Lifecycle, Transaction {
  static Seed<Snapshot> identity(Forum.ID id) {
    return new Snap(
      new Lifecycle(),
      new Model(id)
    );
  }

  record Snapshot(Forum.State state, Forum.Model model) {}

}
