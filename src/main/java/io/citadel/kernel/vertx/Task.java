package io.citadel.kernel.vertx;

import io.citadel.CitadelException;
import io.citadel.kernel.func.ThrowableFunction;
import io.citadel.kernel.func.ThrowablePredicate;
import io.vertx.core.Future;

import java.util.function.Function;

public interface Task {
  default <T> Future<T> success(T result) {
    return Future.succeededFuture(result);
  }

  default <T> Future<T> failure(String message) {
    return Future.failedFuture(new CitadelException(message));
  }

  default <T> Function<T, Future<T>> filter(ThrowablePredicate<? super T> predicate) {
    return filter("Can't solve predicate", predicate);
  }

  default <T> Function<T, Future<T>> filter(String error, ThrowablePredicate<? super T> predicate) {
    return it -> predicate.test(it) ? success(it) : failure(error);
  }
}
