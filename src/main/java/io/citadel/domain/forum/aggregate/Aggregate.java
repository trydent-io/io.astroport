package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.eventstore.data.AggregateInfo;
import io.citadel.eventstore.data.EventInfo;
import io.citadel.kernel.domain.Domain;
import io.citadel.kernel.domain.Eventable;
import io.citadel.kernel.func.ThrowableBiFunction;

import java.util.stream.Stream;

import static io.citadel.domain.forum.aggregate.Aggregate.Type.*;

public sealed interface Aggregate extends Forum<Aggregate>, Domain.Aggregate {
  static Service<Aggregate> root(Model model, long version) {
    return Service.lifecycle(new Root(model, version, Stream.empty()));
  }

  enum Type {
    ;

    private record Root(Model model, long version, Stream<Forum.Event> events) implements Aggregate, Eventable<Forum.Event> {
      @Override
      public Aggregate register(final Name name, final Description description) {
        return new Root(model, version, append(events, Forum.event.registered(name, description, null)));
      }

      @Override
      public Aggregate edit(final Name name, final Description description) {
        return new Root(model, version, append(events, Forum.event.edited(name, description)));
      }

      @Override
      public Aggregate open() {
        return new Root(model, version, append(events, Forum.event.opened(null)));
      }

      @Override
      public Aggregate close() {
        return new Root(model, version, append(events, Forum.event.closed(null)));
      }

      @Override
      public Aggregate archive() {
        return new Root(model, version, append(events, Forum.event.archived(null)));
      }

      @Override
      public Aggregate reopen() {
        return new Root(model, version, append(events, Forum.event.reopened(null)));
      }

      @Override
      public <T> T commit(final ThrowableBiFunction<? super AggregateInfo, ? super Stream<EventInfo>, ? extends T> transaction) {
        return transaction.apply(aggregate(model.id().toString(), "forum", version), events(events));
      }
    }

  }
}
