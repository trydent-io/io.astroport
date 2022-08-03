package io.citadel.kernel.domain;

import io.citadel.kernel.func.TryFunction;
import io.vertx.core.MultiMap;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import static io.citadel.kernel.domain.Headers.Type.Local;

public sealed interface Headers extends Iterable<Map.Entry<String, String>> {
  static Headers of(MultiMap map) {
    return new Local(map);
  }

  <T> Optional<T> find(String key, TryFunction<? super String, ? extends T> then);

  default <ID> ID id(TryFunction<? super String, ? extends ID> then) {return find("id", then).orElseThrow();}
  default String id() {return find("id", it -> it).orElseThrow();}

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
      public <T> Optional<T> find(final String key, final TryFunction<? super String, ? extends T> then) {
        return Optional.ofNullable(origin.get(key)).map(then);
      }
    }
  }
}
