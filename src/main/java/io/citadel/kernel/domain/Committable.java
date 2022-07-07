package io.citadel.kernel.domain;

import io.vertx.core.Future;

public interface Committable {
  Future<Void> commit();
}
