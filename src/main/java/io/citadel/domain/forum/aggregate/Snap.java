package io.citadel.domain.forum.aggregate;

import io.citadel.kernel.func.Maybe;
import io.citadel.kernel.func.ThrowableFunction;

public record Snap(Lifecycle lifecycle, Model model) implements Seed<Seed.Snapshot> {
  @Override
  public Maybe<Seed<Snapshot>> register(Name name, Description description) {
    return lifecycle.register(name, description).map(it -> new Snap(it, new Model(model.id(), new Details(name, description))));
  }

  @Override
  public Maybe<Seed<Snapshot>> replace(Name name, Description description) {
    return lifecycle.replace(name, description).map(it -> new Snap(it, new Model(model.id(), new Details(name, description))));
  }

  @Override
  public Maybe<Seed<Snapshot>> open() {
    return lifecycle.open().map(it -> new Snap(it, model));
  }

  @Override
  public Maybe<Seed<Snapshot>> close() {
    return lifecycle.close().map(it -> new Snap(it, model));
  }

  @Override
  public Maybe<Seed<Snapshot>> archive() {
    return lifecycle.archive().map(it -> new Snap(it, model));
  }

  @Override
  public Maybe<Seed<Snapshot>> reopen() {
    return lifecycle.reopen().map(it -> new Snap(it, model));
  }

  @Override
  public <R> R eventually(ThrowableFunction<? super Snapshot, ? extends R> then) {
    return lifecycle.eventually(it -> then.apply(new Snapshot(it, model)));
  }
}
