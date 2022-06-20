package io.citadel.kernel.domain;

import io.citadel.domain.forum.Forum;
import io.vertx.core.json.JsonObject;

public interface Aggregate<ID, R, E, S extends Enum<S> & State<S, E>> {
  static <ID, R, E, S extends Enum<S> & State<S, E>> Aggregate<ID, R, E, S> root(
    UniProvider<? super String, ? extends ID> id,
    UniProvider<? super ID, ? extends R> model,
    BiProvider<? super String, ? super JsonObject, ? extends E> event,
    BiProvider<? super R, ? super E, ? extends R> attach,
    Provider<? extends S> state
  ) {
    return new Root<>(id, model, event, attach, state);
  }

  UniProvider<? super String, ? extends ID> id();

  UniProvider<? super ID, ? extends R> model();

  BiProvider<? super String, ? super JsonObject, ? extends E> event();

  BiProvider<? super R, ? super E, ? extends R> attach();

  Provider<? extends S> state();

  static void main(String[] args) {
    Aggregate.root(
      Forum::id,
      Forum::model,
      Forum::event,
      Forum::attach,
      Forum::entry
    );
  }
}

record Root<ID, R, E, S extends Enum<S> & State<S, E>>(
  UniProvider<? super String, ? extends ID> id,
  UniProvider<? super ID, ? extends R> model,
  BiProvider<? super String, ? super JsonObject, ? extends E> event,
  BiProvider<? super R, ? super E, ? extends R> attach,
  Provider<? extends S> state
) implements Aggregate<ID, R, E, S> {
}
