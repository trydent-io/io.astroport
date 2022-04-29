package io.citadel.kernel.vertx;

import io.citadel.CitadelException;
import io.vertx.core.Future;

public interface Task {
  default <T> Future<T> success(T result) {
    return Future.succeededFuture(result);
  }

  default <T> Future<T> failure(String message) {
    return Future.failedFuture(new CitadelException(message));
  }
}
