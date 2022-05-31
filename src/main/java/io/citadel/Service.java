package io.citadel;

import io.citadel.kernel.domain.Domain;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Service extends AbstractVerticle implements Citadel.Verticle {
  private static final Logger log = LoggerFactory.getLogger(Citadel.class);

  private final Domain.Verticle domain;

  Service(Domain.Verticle domain) {
    this.domain = domain;
  }

  @Override
  public void start(Promise<Void> start) {

    vertx
      .deployVerticle(Domain.defaults.service(vertx, ))
      .compose(it -> vertx.deployVerticle(domain))
      .<Void>mapEmpty()
      .onSuccess(start::complete)
      .onFailure(start::fail);
  }

}
