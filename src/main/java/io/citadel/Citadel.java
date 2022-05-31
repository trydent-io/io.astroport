package io.citadel;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.media.Json;
import io.citadel.kernel.sql.Database;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.jackson.DatabindCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.util.TimeZone;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static io.vertx.core.Vertx.vertx;

public sealed interface Citadel permits Citadel.Namespace, Citadel.Verticle {
  enum Namespace implements Citadel {;
    private static final Logger log = LoggerFactory.getLogger(Citadel.class);
  }

  static Citadel.Verticle service(Vertx vertx) {
    return new Service(
      Domain.defaults.service(vertx, Database.postgresql("localhost", 5433, "citadel", "citadel", "docker"))
    );
  }

  sealed interface Verticle extends Citadel, io.vertx.core.Verticle permits Service {}

  static void main(String[] args) {
    final var vertx = vertx();

    TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("UTC")));

    DatabindCodec.mapper()
      .registerModule(new JavaTimeModule())
      .disable(FAIL_ON_UNKNOWN_PROPERTIES)
      .disable(WRITE_DATES_AS_TIMESTAMPS);

    vertx
      .deployVerticle(Citadel.service(vertx))
      .onSuccess(it -> Namespace.log.info("Citadel service has been deployed with id %s".formatted(it)))
      .onFailure(it -> Namespace.log.error("Can't deploy Citadel service", it))
      .onFailure(it -> vertx.close());
  }
}
