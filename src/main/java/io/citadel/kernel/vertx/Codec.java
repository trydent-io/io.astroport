package io.citadel.kernel.vertx;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonObject;

public sealed interface Codec<T> extends MessageCodec<T, T> {
  static <R extends Record> Codec<R> forRecord(Class<R> type) {
    return new RecordType<>(type);
  }
}

record RecordType<R extends Record>(Class<R> type) implements Codec<R> {
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

