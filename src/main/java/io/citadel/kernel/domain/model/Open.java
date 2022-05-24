package io.citadel.kernel.domain.model;

import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.func.ThrowableFunction;
import io.citadel.kernel.func.ThrowablePredicate;
import io.citadel.kernel.func.ThrowableSupplier;
import io.vertx.core.Future;

import static java.util.Objects.*;

public final class Open<M extends Record & Domain.Model<?>, E extends Domain.Event> implements Domain.Transaction<M, E> {
  private final M model;
  private final long version;
  private final Domain.Timeline<S, E, ;

  public Open(final M model, final long version, final Domain.Transaction<E> transaction) {
    this.model = model;
    this.version = version;
    this.transaction = transaction;
  }

  @Override
  public Domain.Transaction<M, E> has(final ThrowablePredicate<? super M> condition) {
    return model != null && requireNonNull(condition, "Can't test a null predicate").test(model) ? this : new Open<>(model, version, transaction);
  }

  @Override
  public Domain.Transaction<M, E> log(final ThrowableFunction<? super M, ? extends E> event) {
    return model != null
      ? new Open<>(model, version, transaction.log(requireNonNull(event, "Can't notify a null event").apply(model)))
      : this;
  }

  @Override
  public Domain.Transaction<M, E> log(final ThrowableSupplier<? extends E> event) {
    return model != null
      ? new Open<>(model, version, transaction.log(requireNonNull(event, "Can't notify a null event").get()))
      : this;
  }

  @Override
  public <R extends Domain.Transaction<?, ?>> R let(final ThrowableFunction<? super M, ? extends R> aggregate) {
    return model != null
      ? requireNonNull(aggregate, "Can't supply a null aggregate").apply(model)
      : null;
  }

  @Override
  public Future<Void> commit(final String by) {
    return transaction.commit(model.id().toString(), "", version, by);
  }
}
