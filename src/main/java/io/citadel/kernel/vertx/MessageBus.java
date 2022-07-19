package io.citadel.kernel.vertx;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryContext;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.MessageProducer;

public interface MessageBus {
  <R extends Record> MessageBus send(String address, R message);

  <R extends Record> MessageBus send(String address, R message, DeliveryOptions options);

  <R extends Record, T extends Record> MessageBus request(String address, R message, Handler<AsyncResult<Message<T>>> replyHandler);

  <R extends Record, T extends Record> Future<Message<T>> request(String address, R message);

  <R extends Record, T extends Record> MessageBus request(String address, R message, DeliveryOptions options, Handler<AsyncResult<Message<T>>> replyHandler);

  <R extends Record, T extends Record> Future<Message<T>> request(String address, R message, DeliveryOptions options);

  <R extends Record> MessageBus publish(String address, R message);

  <R extends Record> MessageBus publish(String address, R message, DeliveryOptions options);

  <T extends Record> MessageConsumer<T> consumer(String address);

  <T extends Record> MessageConsumer<T> consumer(String address, Handler<Message<T>> handler);

  <T extends Record> MessageConsumer<T> localConsumer(String address);

  <T extends Record> MessageConsumer<T> localConsumer(String address, Handler<Message<T>> handler);

  <T extends Record> MessageProducer<T> sender(String address);

  <T extends Record> MessageProducer<T> sender(String address, DeliveryOptions options);

  <T extends Record> MessageProducer<T> publisher(String address);

  <T extends Record> MessageProducer<T> publisher(String address, DeliveryOptions options);

  <R extends Record> MessageBus register(Class<R> type);

  <R extends Record> MessageBus unregister(Class<R> type);

  <R extends Record> MessageBus addOutboundInterceptor(Handler<DeliveryContext<R>> interceptor);

  <R extends Record> MessageBus removeOutboundInterceptor(Handler<DeliveryContext<R>> interceptor);

  <R extends Record> MessageBus addInboundInterceptor(Handler<DeliveryContext<R>> interceptor);

  <R extends Record> MessageBus removeInboundInterceptor(Handler<DeliveryContext<R>> interceptor);
}
