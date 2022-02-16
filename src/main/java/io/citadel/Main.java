package io.citadel;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.citadel.kernel.eventstore.EventStore;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Verticle;
import io.vertx.core.json.jackson.DatabindCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.util.TimeZone;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static io.vertx.core.Vertx.vertx;

public final class Main extends AbstractVerticle implements Verticle {
  private static final Logger log = LoggerFactory.getLogger(Main.class);

  private final Citadel citadel;
  private final EventStore eventStore;

  public Main(Citadel citadel) {
    this.citadel = citadel;
  }

  @Override
  public void start(Promise<Void> start) {
    TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("UTC")));

    DatabindCodec.mapper()
      .registerModule(new JavaTimeModule())
      .disable(FAIL_ON_UNKNOWN_PROPERTIES)
      .disable(WRITE_DATES_AS_TIMESTAMPS);

    vertx
      .deployVerticle(eventStore);

    start.complete();
  }

  static void main(String[] args) {
    vertx()
      .deployVerticle(new Main(Citadel.domain()))
      .onSuccess(it -> log.info("Main has been deployed with id %s".formatted(it)))
      .onFailure(it -> log.error("Can't deploy main", it));
  }
}
