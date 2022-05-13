package io.citadel.kernel.vertx;

import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.domain.Headers;
import io.citadel.kernel.func.ThrowableFunction;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;

public interface Actor<A extends Domain.Aggregate> {
  static <ID extends Domain.ID<?>, A extends Domain.Aggregate, S extends Domain.Lookup<ID, ?, A>> Actor<A> create(Vertx vertx, S aggregates, ThrowableFunction<? super String, ? extends ID> id) {
    return new Async<>(vertx, aggregates, id);
  }

  <R extends java.lang.Record> Actor<A> be(Class<R> type, String address, Handler<A, R> handler);

  interface Handler<A extends Domain.Aggregate, R extends java.lang.Record> {
    void handle(Headers headers, Message<R> message, Future<A> aggregate, R behaviour, String by);
  }
}

final class Async<ID extends Domain.ID<?>, A extends Domain.Aggregate, S extends Domain.Lookup<ID, ?, A>> implements Actor<A> {
  private final EventBus eventBus;
  private final S aggregates;
  private final ThrowableFunction<? super String, ? extends ID> id;

  public Async(Vertx vertx, S aggregates, ThrowableFunction<? super String, ? extends ID> id) {
    this.eventBus = vertx.eventBus();
    this.aggregates = aggregates;
    this.id = id;
  }

  @Override
  public <R extends java.lang.Record> Actor<A> be(Class<R> type, String address, Handler<A, R> handler) {
    eventBus
      .registerDefaultCodec(type, Record.codec(type))
      .<R>localConsumer(address, message -> handler.handle(
        Headers.of(message.headers()),
        message,
        aggregate(message),
        message.body(),
        message.headers().get("by"))
      );
    return null;
  }

  private <R extends java.lang.Record> Future<A> aggregate(Message<R> message) {
    return aggregates.aggregate(id.apply(message.headers().get("aggregateId")));
  }
}
