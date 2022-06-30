package io.citadel.kernel.vertx;

import io.citadel.CitadelException;
import io.citadel.kernel.domain.Headers;
import io.citadel.kernel.func.ThrowablePredicate;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;

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

  default <T> Function<T, Future<T>> requireNonNull(String error) {
    return it -> it == null ? failure(error) : success(it);
  }

  interface Handler<R extends Record> extends io.vertx.core.Handler<Message<R>> {
    @Override
    default void handle(Message<R> message) {
      handle(Headers.of(message.headers()), message);
    }

    void handle(Headers headers, Message<R> message);
  }
}
