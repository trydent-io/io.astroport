package io.citadel.domain.topic;

import java.util.Optional;
import java.util.function.Supplier;

public interface Type {
  record Title(String get) implements Supplier<String> {
    static Optional<Title> of(String value) {
      return Optional.ofNullable(value)
        .filter(it -> it.length() >= 3 && it.length() <= 255)
        .map(Title::new);
    }
  }

  record Text(String get) implements Supplier<String> {
    static Optional<Text> of(String value) {
      return Optional.ofNullable(value)
        .filter(it -> it.length() >= 10 && it.length() <= 12000)
        .map(Text::new);
    }
  }
}
