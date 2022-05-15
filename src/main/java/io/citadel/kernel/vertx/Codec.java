package io.citadel.kernel.vertx;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonObject;

interface RecordType<T> extends MessageCodec<T, T> {
  static <R extends java.lang.Record> RecordType<R> codec(Class<R> type) {
    return new Codec<>(type);
  }
}

record Codec<R extends java.lang.Record>(Class<R> type) implements RecordType<R> {
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

