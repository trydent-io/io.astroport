package io.citadel.kernel.domain;

import io.citadel.kernel.vertx.Codec;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;

public interface Context<A extends Domain.Transaction<?, ?>> {
  static <M extends Record & Domain.Model<?>, A extends Domain.Transaction<?, ?>> Context<A> create(Vertx vertx, Domain.Archetype<M> archetype) {
    return new Behavioural<>(vertx, Domain.defaults.lookup(), archetype);
  }

  <R extends Record> Context<A> be(Class<R> type, String address, Behaviour<A, R> handler);

  interface Behaviour<A extends Domain.Transaction<?, ?>, R extends Record> {
    default void be(Headers headers, Message<R> message, A aggregate, R behaviour, String by) {
      be(aggregate, behaviour, by);
    }
    void be(A aggregate, R behaviour, String by);
  }
}

final class Behavioural<M extends Record & Domain.Model<?>, A extends Domain.Transaction<?, ?>> implements Context<A> {
  private final EventBus eventBus;
  private final Domain.Lookup<A> lookup;
  private final Domain.Archetype<M> archetype;

  Behavioural(Vertx vertx, final Domain.Lookup<A> lookup, final Domain.Archetype<M> archetype) {
    this(vertx.eventBus(), lookup, archetype);
  }
  private Behavioural(final EventBus eventBus, final Domain.Lookup<A> lookup, final Domain.Archetype<M> archetype) {
    this.eventBus = eventBus;
    this.lookup = lookup;
    this.archetype = archetype;
  }

  @Override
  public <R extends Record> Context<A> be(Class<R> type, String address, Behaviour<A, R> behaviour) {
    eventBus
      .registerDefaultCodec(type, Codec.forRecord(type))
      .<R>localConsumer(address, message ->
        {
          final var headers = Headers.of(message.headers());
          final var action = message.body();
          final var by = message.headers().get("by");
          final var aggregateId = aggregateId(message);
          lookup
            .findLogs(aggregateId)
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
    return archetype.apply(message.headers().get("aggregateId"));
  }
}
