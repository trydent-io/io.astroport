package io.citadel.domain.topic;

import io.citadel.shared.domain.Attribute.FromString;
import jakarta.persistence.Converter;

public interface Attribute {
  @Converter
  final class AsTitle extends FromString<Type.Title> {
    public AsTitle() {
      super(it -> Type.Title.of(it).orElseThrow(() -> new IllegalArgumentException("Can't hydrate Title value")));
    }
  }

  @Converter
  final class AsText extends FromString<Type.Text> {
    public AsText() {
      super(it -> Type.Text.of(it).orElseThrow(() -> new IllegalArgumentException("Can't hydrate Text value")));
    }
  }
}
