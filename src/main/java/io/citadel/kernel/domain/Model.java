package io.citadel.kernel.domain;

import io.citadel.kernel.domain.context.Bounded;
import io.citadel.kernel.eventstore.Entities;
import io.citadel.kernel.eventstore.metadata.MetaAggregate;
import io.citadel.kernel.eventstore.audit.Name;
import io.citadel.kernel.eventstore.audit.Version;
import io.citadel.kernel.func.ThrowableFunction;
import io.citadel.kernel.func.ThrowableSupplier;
import io.citadel.kernel.vertx.RecordType;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.util.function.Function;

sealed public interface Model<T, R extends Record, F, N extends Enum<N> & State<N, F>> extends Domain {

  <B extends Record> io.citadel.kernel.domain.Model<T, R, F, N> handle(Class<B> type, Handler<T, R, F, N, B> handler);

  io.citadel.kernel.domain.Model<T, R, F, N> aggregate(
    ThrowableFunction<? super String, ? extends T> asId,
    ThrowableFunction<? super JsonObject, ? extends R> asModel,
    ThrowableSupplier<? extends N> asInitial,
    ThrowableFunction<? super String, ? extends N> asState
  );

  Entities bind(Entities pool);

  final class Local<T, R extends Record, F, N extends Enum<N> & State<N, F>> implements io.citadel.kernel.domain.Model<T, R, F, N> {
    private final Vertx vertx;
    private final Name name;
    private final Entities pool;
    private final ThrowableFunction<? super String, ? extends T> aggregateId;
    private final ThrowableFunction<? super JsonObject, ? extends R> aggregateEntity;
    private final ThrowableSupplier<? extends N> initialState;
    private final ThrowableFunction<? super String, ? extends N> aggregateState;

    public Local(Vertx vertx, Name name, Entities pool) {
      this(vertx, name, pool, null, null, null, null);
    }

    private Local(Vertx vertx, Name name, Entities pool, ThrowableFunction<? super String, ? extends T> aggregateId, ThrowableFunction<? super JsonObject, ? extends R> aggregateEntity, ThrowableSupplier<? extends N> initialState, ThrowableFunction<? super String, ? extends N> aggregateState) {
      this.vertx = vertx;
      this.name = name;
      this.pool = pool;
      this.aggregateId = aggregateId;
      this.aggregateEntity = aggregateEntity;
      this.initialState = initialState;
      this.aggregateState = aggregateState;
    }

    @Override
    public <B extends Record> io.citadel.kernel.domain.Model<T, R, F, N> handle(Class<B> type, Handler<T, R, F, N, B> handler) {
      vertx
        .eventBus()
        .registerDefaultCodec(type, RecordType.codec(type))
        .localConsumer("%s.%s".formatted(name, type.getSimpleName())).han, local(handler));
      return this;
    }

    private <B extends Record> io.vertx.core.Handler<Message<B>> local(Handler<T, R, F, N, B> handler) {
      return message -> pool.query(MetaAggregate.id(Headers.of(message.headers()).id(aggregateId)), name)
        .map(asAggregateRoot())
        .map(asContext(handler, message))
        .onSuccess(bounded -> bounded.commit())
        .onFailure(throwable -> message.fail(500, error(message, throwable)));
    }

    private <B extends Record> Function<MetaAggregate, Bounded<MetaAggregate.Root<T, R, F, N>>> asContext(Handler<T, R, F, N, B> handler, Message<B> message) {
      return aggregate ->
        handler.handle(
          Headers.of(message.headers()),
          message,
          Bounded.open(Transaction.of() aggregate),
          message.body()
        );
    }

    private Function<MetaAggregate, MetaAggregate> asAggregateRoot() {
      return aggregate ->
        switch (aggregate) {
          case MetaAggregate.Zero zero -> aggregateRoot(zero);
          case MetaAggregate.Last last -> aggregateRoot(last);
          default -> throw new IllegalStateException("Can't transform an aggregate-root to itself");
        };
    }

    private MetaAggregate aggregateRoot(MetaAggregate.Last last) {
      return MetaAggregate.root(
        last.id().as(aggregateId),
        last.entity().as(aggregateEntity),
        last.state().as(aggregateState),
        last.version()
      );
    }

    private MetaAggregate aggregateRoot(MetaAggregate.Zero zero) {
      return MetaAggregate.root(
        zero.id().as(aggregateId),
        null,
        initialState.get(),
        Version.Zero
      );
    }

    @Override
    public io.citadel.kernel.domain.Model<T, R, F, N> aggregate(ThrowableFunction<? super String, ? extends T> asId, ThrowableFunction<? super JsonObject, ? extends R> asModel, ThrowableSupplier<? extends N> asInitial, ThrowableFunction<? super String, ? extends N> asState) {
      return null;
    }

    @Override
    public Entities bind(Entities pool) {
      return null;
    }
  }
}
