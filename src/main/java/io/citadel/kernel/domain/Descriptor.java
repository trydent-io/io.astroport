package io.citadel.kernel.domain;

import io.citadel.domain.forum.Forum;
import io.vertx.core.json.JsonObject;

public interface Descriptor<ID, R, E, S extends Enum<S> & State<S, E>> {
  static <ID, R, E, S extends Enum<S> & State<S, E>> Descriptor<ID, R, E, S> aggregate(
    UniProvider<? super String, ? extends ID> id,
    UniProvider<? super ID, ? extends R> model,
    BiProvider<? super String, ? super JsonObject, ? extends E> event,
    BiProvider<? super R, ? super E, ? extends R> attach,
    Provider<? extends S> state
  ) {
    return new Root<>(id, model, event, attach, state);
  }

  ID id(String value);
  R entity(ID id);
  E event(String name, JsonObject json);
  R attach(R entity, E event);
  S entry();

  static void main(String[] args) {
    aggregate(
      Forum::id,
      Forum::entity,
      Forum::event,
      Forum::attach,
      Forum::entryPoint
    );
  }
}

record Root<ID, R, E, S extends Enum<S> & State<S, E>>(
  UniProvider<? super String, ? extends ID> id,
  UniProvider<? super ID, ? extends R> entity,
  BiProvider<? super String, ? super JsonObject, ? extends E> event,
  BiProvider<? super R, ? super E, ? extends R> attach,
  Provider<? extends S> state
) implements Descriptor<ID, R, E, S> {
  @Override
  public ID id(String value) {
    return id.apply(value);
  }
  @Override
  public R entity(ID id) {
    return entity.apply(id);
  }
  @Override
  public E event(String name, JsonObject json) {
    return event.apply(name, json);
  }
  @Override
  public R attach(R entity, E event) {
    return attach.apply(entity, event);
  }
  @Override
  public S entry() {
    return state.get();
  }
}
