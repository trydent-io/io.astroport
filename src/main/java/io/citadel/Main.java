package io.citadel;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.citadel.eventstore.EventStore;
import io.citadel.shared.db.Database;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Verticle;
import io.vertx.core.json.jackson.DatabindCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Driver;
import java.time.ZoneId;
import java.util.TimeZone;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static io.vertx.core.Vertx.vertx;

public final class Main extends AbstractVerticle implements Verticle {
  private static final Logger log = LoggerFactory.getLogger(Main.class);

  private final Citadel citadel;
  private final EventStore eventStore;

  public Main(Citadel citadel, EventStore eventStore) {
    this.citadel = citadel;
    this.eventStore = eventStore;
  }

  @Override
  public void start(Promise<Void> start) {
    TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("UTC")));

    DatabindCodec.mapper()
      .registerModule(new JavaTimeModule())
      .disable(FAIL_ON_UNKNOWN_PROPERTIES)
      .disable(WRITE_DATES_AS_TIMESTAMPS);

    vertx
      .deployVerticle(eventStore.asVerticle())
      .compose(it -> vertx.deployVerticle(citadel.asVerticle()));

    start.complete();
  }

  static void main(String[] args) {
    final var vertx = vertx();
    vertx
      .deployVerticle(
        new Main(
          Citadel.domain(),
          EventStore.service(vertx, new Database.Info<>("eventstore", "postgres", "docker", "jdbc:psql://localhost", 5432, Driver.class))))
      .onSuccess(it -> log.info("Main service has been deployed with id %s".formatted(it)))
      .onFailure(it -> log.error("Can't deploy main service", it));
  }
}
