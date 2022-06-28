package io.citadel.kernel.domain;

import io.citadel.domain.forum.Forum;

public interface Descriptor<ID, R, E, S extends Enum<S> & State<S, E>> {
  static <ID, R, E, S extends Enum<S> & State<S, E>> Descriptor<ID, R, E, S> aggregate(
    UniProvider<? super String, ? extends ID> id,
    UniProvider<? super ID, ? extends R> entity,
    UniProvider<? super String, ? extends S> state
  ) {
    return new Root<>(id, entity, state);
  }

  ID id(String value);
  R entity(ID id);
  S state(String value);

  static void main(String[] args) {
    aggregate(
      Forum::id,
      Forum::model,
      Forum::state
    );
  }
}

record Root<ID, R, E, S extends Enum<S> & State<S, E>>(
  UniProvider<? super String, ? extends ID> id,
  UniProvider<? super ID, ? extends R> entity,
  UniProvider<? super String, ? extends S> state
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
  public S state(String value) {
    return state.apply(value);
  }
}
