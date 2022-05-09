package io.citadel.domain.forum;

import io.citadel.domain.forum.aggregate.Defaults;
import io.citadel.domain.forum.aggregate.Hydration;
import io.citadel.domain.forum.aggregate.Root;
import io.citadel.domain.forum.aggregate.Staging;
import io.citadel.domain.forum.handler.Commands;
import io.citadel.domain.forum.handler.Events;
import io.citadel.domain.forum.model.Attributes;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.domain.Headers;
import io.citadel.kernel.domain.attribute.Attribute;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.util.UUID;

public sealed interface Forum<F extends Forum<F>> permits Forum.Aggregate, Forum.Lifecycle, Forum.Snapshot {
  String AGGREGATE_NAME = "FORUM";

  Commands commands = Commands.Companion;
  Events events = Events.Companion;
  Attributes attributes = Attributes.Companion;
  Defaults defaults = Defaults.Companion;

  enum State implements Domain.State<Forum.State> {Registered, Open, Closed, Archived}

  sealed interface Command extends Domain.Command permits Commands.Replace, Commands.Archive, Commands.Close, Commands.Open, Commands.Register, Commands.Reopen {}
  sealed interface Event extends Domain.Event permits Events.Archived, Events.Closed, Events.Replaced, Events.Opened, Events.Registered, Events.Reopened {}

  sealed interface Aggregate extends Forum<Aggregate>, Domain.Aggregate permits Root {}
  sealed interface Lifecycle extends Forum<Lifecycle> permits Staging {}
  sealed interface Snapshot extends Forum<Snapshot>, Domain.Snapshot<Forum.Model, Forum.Aggregate, Forum.Snapshot> permits Hydration {}

  record ID(UUID value) implements Domain.ID<UUID> {} // ID
  record Name(String value) implements Attribute<String> {} // part of Details
  record Description(String value) implements Attribute<String> {} // part of Details
  record Details(Name name, Description description) {} // ValueObject for Details

  Future<F> register(Forum.Details details);
  Future<F> replace(Forum.Details details);
  Future<F> open();
  Future<F> close();
  Future<F> archive();
  Future<F> reopen();

  record Model(ID id, Details details) implements Domain.Model<Forum.ID> {
    public Model(ID id) {this(id, null);}
  }

  interface Handler<S extends Record> extends Domain.Handler<S> {
    @Override
    default void handle(final Message<JsonObject> message, final String aggregateId, final S content, final String by, final Headers headers) {
      handle(message, headers.aggregateId(Forum.attributes::id).orElseThrow(), content, by, headers);
    }

    void handle(Message<JsonObject> message, Forum.ID forumId, S content, String by, Headers headers);
  }
}

