package io.citadel;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.citadel.domain.Context;
import io.citadel.eventstore.EventStore;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.jackson.DatabindCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.util.TimeZone;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

public final class Service extends AbstractVerticle implements Citadel {
  private static final Logger log = LoggerFactory.getLogger(Citadel.class);

  private final Context context;
  private final EventStore eventStore;

  Service(EventStore eventStore, Context context) {
    this.context = context;
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
      .compose(it -> vertx.deployVerticle(context.asVerticle()))
      .<Void>mapEmpty()
      .onSuccess(start::complete)
      .onFailure(start::fail);
  }

}
