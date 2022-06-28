package io.citadel.kernel.domain;

import io.vertx.core.Future;

public interface Migration {
  Future<Void> migrate();
}
