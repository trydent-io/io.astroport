package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.citadel.kernel.domain.Domain;

import static io.citadel.domain.forum.aggregate.Snap.Type.*;

public sealed interface Snap extends Forum<Snap>, Domain.Snapshot<Aggregate> {
  static Snap shot(Forum.ID id) {
    return new Lifecycle(Life.span(new Shot(new Model(id, null))));
  }

  enum Type {;
    private record Shot(Model model) implements Snap {
      @Override
      public Snap register(final Name name, final Description description) {
        return new Shot(new Model(model.id(), new Details(name, description)));
      }

      @Override
      public Snap edit(final Name name, final Description description) {
        return new Shot(new Model(model.id(), new Details(name, description)));
      }

      @Override
      public Snap open() {
        return this;
      }

      @Override
      public Snap close() {
        return this;
      }

      @Override
      public Snap archive() {
        return this;
      }

      @Override
      public Snap reopen() {
        return this;
      }

      @Override
      public Aggregate aggregate(final long version) {
        return Aggregate.root(model, version);
      }
    }

    private record Lifecycle(Life<Snap> life) implements Snap {
      @Override
      public Snap register(final Name name, final Description description) {
        return new Lifecycle(life.register(name, description));
      }

      @Override
      public Snap edit(final Name name, final Description description) {
        return new Lifecycle(life.edit(name, description));
      }

      @Override
      public Snap open() {
        return new Lifecycle(life.open());
      }

      @Override
      public Snap close() {
        return new Lifecycle(life.close());
      }

      @Override
      public Snap archive() {
        return new Lifecycle(life.archive());
      }

      @Override
      public Snap reopen() {
        return new Lifecycle(life.reopen());
      }

      @Override
      public Aggregate aggregate(final long version) {
        return life.eventually(snap -> snap.aggregate(version));
      }
    }
  }
}
