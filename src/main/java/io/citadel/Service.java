package io.citadel;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.citadel.domain.forum.handler.Commands;
import io.citadel.kernel.domain.Domain;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.util.TimeZone;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

public final class Service extends AbstractVerticle implements Citadel.Verticle {
  private static final Logger log = LoggerFactory.getLogger(Citadel.class);

  private final Domain.Verticle domain;

  Service(Domain.Verticle domain) {
    this.domain = domain;
  }

  @Override
  public void start(Promise<Void> start) {
    TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("UTC")));

    DatabindCodec.mapper()
      .registerModule(new JavaTimeModule())
      .disable(FAIL_ON_UNKNOWN_PROPERTIES)
      .disable(WRITE_DATES_AS_TIMESTAMPS);

    vertx.eventBus()
      .registerDefaultCodec(Commands.Register.class, new MessageCodec<Commands.Register, Commands.Register>() {
        @Override
        public void encodeToWire(Buffer buffer, Commands.Register register) {
          buffer.appendBuffer(JsonObject.mapFrom(register).toBuffer());
        }

        @Override
        public Commands.Register decodeFromWire(int i, Buffer buffer) {
          return buffer.toJsonObject().mapTo(Commands.Register.class);
        }

        @Override
        public Commands.Register transform(Commands.Register register) {
          return register;
        }

        @Override
        public String name() {
          return "";
        }

        @Override
        public byte systemCodecID() {
          return 1;
        }
      });

    vertx
      .deployVerticle(eventStore)
      .compose(it -> vertx.deployVerticle(domain))
      .<Void>mapEmpty()
      .onSuccess(start::complete)
      .onFailure(start::fail);
  }

}
