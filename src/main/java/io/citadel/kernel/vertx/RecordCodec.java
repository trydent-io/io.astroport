package io.citadel.kernel.vertx;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.Json;

public record RecordCodec<R extends Record>(Class<R> type, String name, byte systemCodecID) implements MessageCodec<R, R> {
  public static <R extends Record> RecordCodec<R> of(Class<R> type) {
    return new RecordCodec<>(type, type.getCanonicalName(), (byte) -1);
  }

  @Override
  public void encodeToWire(Buffer buffer, R record) {
    buffer.appendBuffer(Json.encodeToBuffer(record));
  }

  @Override
  public R decodeFromWire(int i, Buffer buffer) {
    return buffer.toJsonObject().mapTo(type);
  }

  @Override
  public R transform(R record) {
    return record;
  }
}

