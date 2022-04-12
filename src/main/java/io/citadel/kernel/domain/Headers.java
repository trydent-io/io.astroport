package io.citadel.kernel.domain;

import io.citadel.kernel.func.ThrowableFunction;
import io.vertx.core.MultiMap;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import static io.citadel.kernel.domain.Headers.Type.Local;

public sealed interface Headers extends Iterable<Map.Entry<String, String>> {
  static Headers of(MultiMap map) {
    return new Local(map);
  }

  <T> Optional<T> find(String key, ThrowableFunction<? super String, ? extends T> then);

  default <I extends Domain.ID> Optional<I> aggregateId(ThrowableFunction<? super String, ? extends I> then) {return find("aggregateId", then);}

  enum Type {
    ;

    static final class Local implements Headers {
      private final MultiMap origin;

      Local(final MultiMap origin) {this.origin = origin;}

      @Override
      public Iterator<Map.Entry<String, String>> iterator() {
        return origin.iterator();
      }

      @Override
      public <T> Optional<T> find(final String key, final ThrowableFunction<? super String, ? extends T> then) {
        return Optional.ofNullable(origin.get(key)).map(then);
      }
    }
  }
}
