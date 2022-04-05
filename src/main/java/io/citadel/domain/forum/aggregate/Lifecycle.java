package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.kernel.domain.Domain;

public sealed interface Lifecycle<F extends Forum<F>> extends Forum<Lifecycle<F>>, Domain.Lifecycle<F> permits Service {
  static <F extends Forum<F>> Lifecycle<F> service(F forum) {
    return new Service<>(null, forum);
  }
}
