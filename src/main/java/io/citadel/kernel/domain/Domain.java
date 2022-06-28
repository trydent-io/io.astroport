package io.citadel.kernel.domain;

import io.citadel.kernel.domain.context.Context;
import io.citadel.kernel.domain.model.Defaults;
import io.citadel.kernel.domain.model.Service;
import io.citadel.kernel.eventstore.EventStorePool;
import io.citadel.kernel.eventstore.metadata.Aggregate;
import io.citadel.kernel.eventstore.metadata.Aggregate.Root;
import io.citadel.kernel.eventstore.metadata.ID;
import io.citadel.kernel.eventstore.metadata.Name;
import io.citadel.kernel.eventstore.metadata.Version;
import io.citadel.kernel.func.ThrowableFunction;
import io.citadel.kernel.func.ThrowableSupplier;
import io.citadel.kernel.vertx.Codec;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.util.function.Function;

public interface Domain {
  Defaults defaults = Defaults.Companion;

  sealed interface Verticle extends Domain, io.vertx.core.Verticle permits Service {
  }

  static <T, R extends Record, F, N extends Enum<N> & State<N, F>> Model<T, R, F, N> model(String name) {
    return new Model.Impl<>(Name.of(name));
  }

  interface Handler<ID, M extends Record, E, S extends Enum<S> & State<S, E>, R extends Record> {
    Context<Root<ID, M, E, S>> handle(Headers headers, Message<R> message, Context<Root<ID, M, E, S>> context, R command);
  }

  sealed interface Model<T, R extends Record, F, N extends Enum<N> & State<N, F>> extends Domain {

    <B extends Record> Model<T, R, F, N> handle(Class<B> type, Domain.Handler<T, R, F, N, B> handler);

    Model<T, R, F, N> aggregate(
      ThrowableFunction<? super String, ? extends T> asId,
      ThrowableFunction<? super JsonObject, ? extends R> asModel,
      ThrowableSupplier<? extends N> asInitial,
      ThrowableFunction<? super String, ? extends N> asState
    );

    EventStorePool bind(EventStorePool pool);

    final class Impl<T, R extends Record, F, N extends Enum<N> & State<N, F>> implements Model<T, R, F, N> {
      private final Vertx vertx;
      private final Name name;
      private final EventStorePool pool;
      private final ThrowableFunction<? super String, ? extends T> asId;
      private final ThrowableFunction<? super JsonObject, ? extends R> asEntity;
      private final ThrowableSupplier<? extends N> asInitial;
      private final ThrowableFunction<? super String, ? extends N> asState;

      public Impl(Vertx vertx, Name name, EventStorePool pool) {
        this(vertx, name, pool, null, null, null, null);
      }

      private Impl(Vertx vertx, Name name, EventStorePool pool, ThrowableFunction<? super String, ? extends T> asId, ThrowableFunction<? super JsonObject, ? extends R> asEntity, ThrowableSupplier<? extends N> asInitial, ThrowableFunction<? super String, ? extends N> asState) {
        this.vertx = vertx;
        this.name = name;
        this.pool = pool;
        this.asId = asId;
        this.asEntity = asEntity;
        this.asInitial = asInitial;
        this.asState = asState;
      }

      @Override
      public <B extends Record> Model<T, R, F, N> handle(Class<B> type, Handler<T, R, F, N, B> handler) {
        vertx.eventBus().registerDefaultCodec(type, Codec.forRecord(type)).localConsumer("%s.%s".formatted(name, type.getSimpleName()), local(handler));
        return this;
      }

      private <B extends Record> io.vertx.core.Handler<Message<B>> local(Handler<T, R, F, N, B> handler) {
        return message -> pool.query(Aggregate.id(Headers.of(message.headers()).id(asId)), name)
          .map(asAggregateRoot())
          .map(asContext(handler, message))
          .onSuccess(context -> context.commit())
          .onFailure(throwable -> message.fail(500, error(message, throwable)));
      }

      private <B extends Record> Function<Aggregate, Context<Root<T, R, F, N>>> asContext(Handler<T, R, F, N, B> handler, Message<B> message) {
        return aggregate ->
          handler.handle(
            Headers.of(message.headers()),
            message,
            Context.bound(aggregate),
            message.body()
          );
      }

      private Function<Aggregate, Aggregate> asAggregateRoot() {
        return aggregate ->
          switch (aggregate) {
            case Aggregate.Zero zero -> Aggregate.root(zero.id().as(asId), null, asInitial.get(), Version.Zero);
            case Aggregate.Last last -> Aggregate.root(last.id().as(asId), last.entity().as(asEntity), last.state().as(asState), last.version());
            default -> aggregate;
          };
      }

      @Override
      public Model<T, R, F, N> aggregate(ThrowableFunction<? super String, ? extends T> asId, ThrowableFunction<? super JsonObject, ? extends R> asModel, ThrowableSupplier<? extends N> asInitial, ThrowableFunction<? super String, ? extends N> asState) {
        return null;
      }

      @Override
      public EventStorePool bind(EventStorePool pool) {
        return null;
      }
    }
  }
}

