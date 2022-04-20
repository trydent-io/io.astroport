package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.kernel.func.ThrowableFunction;

public record Span<F extends Forum<F>> implements Forum<F> permits Timepoint, Transaction {
  protected final Lifecycle<F> service;
  private final ThrowableFunction<? super Lifecycle<F>, ? extends F> next;

  protected Span(Lifecycle<F> service, ThrowableFunction<? super Lifecycle<F>, ? extends F> next) {
    this.service = service;
    this.next = next;
  }

  @Override
  public final F register(Name name, Description description) {
    return next.apply(service.register(name, description));
  }

  @Override
  public final F replace(Name name, Description description) {
    return next.apply(service.replace(name, description));
  }

  @Override
  public final F open() {
    return next.apply(service.open());
  }

  @Override
  public final F close() {
    return next.apply(service.close());
  }

  @Override
  public final F archive() {
    return next.apply(service.archive());
  }

  @Override
  public final F reopen() {
    return next.apply(service.reopen());
  }
}
