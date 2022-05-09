package io.citadel.kernel.vertx;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonObject;

public interface MessageBus {
  static MessageBus create(Vertx vertx) {
    return new Omnibus(vertx.eventBus());
  }

  <R extends Record> MessageBus subscribe(Class<R> record, Handler<io.vertx.core.eventbus.Message<R>> handler);

  <R extends Record> MessageBus publish(R record);
}

interface Codec<R extends Record> extends MessageCodec<R, R> {
  static <R extends Record> Codec<R> of(Class<R> type) {
    return new Handlers<>(type);
  }
}

record Message<R extends Record>(Class<R> type) implements Codec<R> {
  @Override
  public void encodeToWire(Buffer buffer, R record) {
    buffer.appendBuffer(JsonObject.mapFrom(record).toBuffer());
  }

  @Override
  public R decodeFromWire(int i, Buffer buffer) {
    return buffer.toJsonObject().mapTo(type);
  }

  @Override
  public R transform(R record) {
    return record;
  }

  @Override
  public String name() {
    return "none";
  }

  @Override
  public byte systemCodecID() {
    return -1;
  }
}

final class Omnibus implements MessageBus {
  private final EventBus eventBus;

  Omnibus(final EventBus eventBus) {
    this.eventBus = eventBus;
  }

  @Override
  public <R extends Record> MessageBus subscribe(Class<R> record, Handler<io.vertx.core.eventbus.Message<R>> handler) {
    eventBus
      .registerDefaultCodec(record, Codec.of(record))
      .localConsumer(record.getSimpleName(), handler);
    return this;
  }

  @Override
  public <R extends Record> MessageBus publish(R record) {
    eventBus.publish(record.getClass().getSimpleName(), record);
    return this;
  }
}
