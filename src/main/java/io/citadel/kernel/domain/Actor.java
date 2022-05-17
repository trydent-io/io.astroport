package io.citadel.kernel.domain;

import io.citadel.kernel.func.ThrowableFunction;
import io.citadel.kernel.vertx.RecordType;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;

public interface Actor<ID extends Domain.ID<?>, M extends Record & Domain.Model<ID>, A extends Domain.Aggregate> {
  static <ID extends Domain.ID<?>, M extends Record & Domain.Model<ID>, A extends Domain.Aggregate> Actor<ID, M, A> create(Vertx vertx, Domain.Lookup<M, A> lookup, ThrowableFunction<? super String, ? extends ID> id) {
    return new Behavioural<>(vertx, lookup, id);
  }

  default <R extends Record> Actor<ID, M, A> be(Class<R> type, String address, Behaviour<A, R> handler) {
    return be(type, address, it -> true, handler);
  }

  <R extends Record> Actor<ID, M, A> be(Class<R> type, String address, Assertion<M> assertion, Behaviour<A, R> handler);

  interface Assertion<M extends Record & Domain.Model<?>> {
    boolean assertThat(M model);
  }

  interface Behaviour<A extends Domain.Aggregate, R extends Record> {
    default void be(Headers headers, Message<R> message, A aggregate, R behaviour, String by) {
      be(aggregate, behaviour, by);
    }

    void be(A aggregate, R behaviour, String by);
  }
}

final class Behavioural<ID extends Domain.ID<?>, M extends Record & Domain.Model<ID>, A extends Domain.Aggregate> implements Actor<ID, M, A> {
  private final EventBus eventBus;
  private final Domain.Lookup<M, A> lookup;
  private final ThrowableFunction<? super String, ? extends ID> id;

  Behavioural(Vertx vertx, final Domain.Lookup<M, A> lookup, final ThrowableFunction<? super String, ? extends ID> id) {
    this(vertx.eventBus(), lookup, id);
  }

  Behavioural(final EventBus eventBus, final Domain.Lookup<M, A> lookup, final ThrowableFunction<? super String, ? extends ID> id) {
    this.eventBus = eventBus;
    this.lookup = lookup;
    this.id = id;
  }

  @Override
  public <R extends Record> Actor<ID, M, A> be(Class<R> type, String address, Assertion<M> assertion, Behaviour<A, R> behaviour) {
    eventBus
      .registerDefaultCodec(type, RecordType.codec(type))
      .<R>localConsumer(address, message ->
        {
          final var headers = Headers.of(message.headers());
          final var action = message.body();
          final var by = message.headers().get("by");
          lookup
            .findAggregate(aggregateId(message), assertion::assertThat)
            .onSuccess(aggregate -> behaviour.be(
                headers,
                message,
                aggregate,
                action,
                by
              )
            )
            .onFailure(throwable -> message.fail(500, error(message, throwable)));
        }
      );
    return this;
  }

  private <R extends Record> String error(final Message<R> message, final Throwable throwable) {
    return "Can't lookup aggregate %s, because of %s".formatted(message.headers().get("aggregateId"), throwable.getMessage());
  }

  private <R extends Record> ID aggregateId(final Message<R> message) {
    return id.apply(message.headers().get("aggregateId"));
  }

}
