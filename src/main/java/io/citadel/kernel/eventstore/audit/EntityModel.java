package io.citadel.kernel.eventstore.audit;

import io.citadel.kernel.eventstore.metadata.Model;
import io.citadel.kernel.eventstore.metadata.State;
import io.vertx.core.json.JsonObject;

import java.util.function.Function;

public enum EntityModel {
  Companion;

  public Entity with(String id, String name, long version) {
    return new Entity(Entity.id(id), Entity.name(name), Entity.version(version));
  }

  public record Entity(ID id, Name name, Version version) {

    static <T> Entity of(T id, String name, long version) {
      return new Entity(id(id), name(name), version(version));
    }

    static <T> ID id(T value) { return ID.of(value.toString()); }
    static Name name(String value) {
      return Name.of(value);
    }
    static Version version(Long value) { return Version.of(value); }
    static State state(String value) { return State.of(value); }
    static Model model(JsonObject value) {
      return Model.of(value);
    }

    public record ID(String value) {
      public ID {
        assert value != null;
      }
      public <T> T as(Function<? super String, ? extends T> deserializer) {
        return deserializer.apply(value);
      }

      public static ID of(String value) {
        return switch (value) {
          case null -> throw new IllegalArgumentException("ID can't be null");
          default -> new ID(value);
        };
      }
    }

  }
}
