package io.citadel.kernel.domain.model;

import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.func.ThrowableFunction;
import io.citadel.kernel.func.ThrowablePredicate;
import io.citadel.kernel.func.ThrowableSupplier;
import io.vertx.core.Future;

import static java.util.Objects.*;

public final class Root<M extends Record & Domain.Model<?>, E extends Domain.Event> implements Domain.Aggregate<M, E> {
  private final M model;
  private final long version;
  private final Domain.Transaction<E> transaction;

  public Root(final M model, final long version, final Domain.Transaction<E> transaction) {
    this.model = model;
    this.version = version;
    this.transaction = transaction;
  }

  @Override
  public Domain.Aggregate<M, E> asserts(final ThrowablePredicate<? super M> where) {
    return model != null && requireNonNull(where, "Can't test a null predicate").test(model) ? this : new Root<>(model, version, transaction);
  }

  @Override
  public Domain.Aggregate<M, E> notify(final ThrowableFunction<? super M, ? extends E> event) {
    return model != null
      ? new Root<>(model, version, transaction.log(requireNonNull(event, "Can't notify a null event").apply(model)))
      : this;
  }

  @Override
  public Domain.Aggregate<M, E> notify(final ThrowableSupplier<? extends E> event) {
    return model != null
      ? new Root<>(model, version, transaction.log(requireNonNull(event, "Can't notify a null event").get()))
      : this;
  }

  @Override
  public <R extends Domain.Aggregate<?, ?>> R supply(final ThrowableFunction<? super M, ? extends R> aggregate) {
    return model != null
      ? requireNonNull(aggregate, "Can't supply a null aggregate").apply(model)
      : null;
  }

  @Override
  public Future<Void> submit(final String by) {
    return transaction.commit(model.id().toString(), "", version, by);
  }
}
