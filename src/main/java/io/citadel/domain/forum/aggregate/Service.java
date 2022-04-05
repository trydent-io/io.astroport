package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.kernel.domain.Domain;

public sealed interface Service<F extends Forum<F>> extends Forum<Service<F>>, Domain.Service<F> permits Lifecycle {
  static <F extends Forum<F>> Service<F> lifecycle(F forum) {
    return new Lifecycle<>(null, forum);
  }
}
