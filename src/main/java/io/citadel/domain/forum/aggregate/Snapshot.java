package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.kernel.domain.Domain;

public sealed interface Snapshot extends Forum<Snapshot>, Domain.Snapshot<Aggregate> permits Hydration, Timepoint {
  static Snapshot hydration() {
    return new Hydration(null);
  }

  static Snapshot timepoint(Lifecycle<Snapshot> lifecycle) {
    return new Timepoint(lifecycle);
  }
}
