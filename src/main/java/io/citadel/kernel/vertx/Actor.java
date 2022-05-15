package io.citadel.kernel.vertx;

import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.domain.Headers;
import io.citadel.kernel.func.ThrowableFunction;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;

public interface Actor<A extends Domain.Aggregate> {
  static <ID extends Domain.ID<?>, A extends Domain.Aggregate> Actor<A> create(Vertx vertx, Domain.Lookup<ID, ?, A> lookup, ThrowableFunction<? super String, ? extends ID> id) {
    return new Async<>(vertx, lookup, id);
  }

  <R extends Record> Actor<A> be(Class<R> type, String address, Handler<A, R> handler);

  interface Handler<A extends Domain.Aggregate, R extends Record> {
    void handle(Headers headers, Message<R> message, A aggregate, R behaviour, String by);
  }

  interface Behaviour<A extends Domain.Aggregate, R extends Record> extends Handler<A, R> {
    @Override
    default void handle(Headers headers, Message<R> message, A aggregate, R behaviour, String by) {
      handle(aggregate, behaviour, by);
    }

    void handle(A aggregate, R behaviour, String by);
  }
}

final class Async<ID extends Domain.ID<?>, A extends Domain.Aggregate> implements Actor<A> {
  private final EventBus eventBus;
  private final Domain.Lookup<ID, ?, A> lookup;
  private final ThrowableFunction<? super String, ? extends ID> id;

  Async(Vertx vertx, final Domain.Lookup<ID, ?, A> lookup, final ThrowableFunction<? super String, ? extends ID> id) {
    this(vertx.eventBus(), lookup, id);
  }

  Async(final EventBus eventBus, final Domain.Lookup<ID, ?, A> lookup, final ThrowableFunction<? super String, ? extends ID> id) {
    this.eventBus = eventBus;
    this.lookup = lookup;
    this.id = id;
  }

  @Override
  public <R extends Record> Actor<A> be(Class<R> type, String address, Handler<A, R> handler) {
    eventBus
      .registerDefaultCodec(type, RecordType.codec(type))
      .<R>localConsumer(address, message ->
        lookup
          .findAggregate(aggregateId(message))
          .onSuccess(aggregate -> handler.handle(
            Headers.of(message.headers()),
            message,
            aggregate,
            message.body(),
            message.headers().get("by"))
          )
          .onFailure(throwable -> message.fail(500, error(message, throwable)))
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
