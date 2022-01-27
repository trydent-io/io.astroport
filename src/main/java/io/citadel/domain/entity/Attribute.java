package io.citadel.domain.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Attribute {
  abstract class FromString<T extends Supplier<String>> implements AttributeConverter<T, String> {
    private final Function<String, T> asT;

    protected FromString(final Function<String, T> asT) {this.asT = asT;}

    @Override
    public final String convertToDatabaseColumn(T attribute) { return attribute.get(); }

    @Override
    public final T convertToEntityAttribute(String value) { return asT.apply(value); }
  }

  abstract class FromNumber<N extends Number, T extends Supplier<N>> implements AttributeConverter<T, N> {
    private final Function<N, T> asT;

    protected FromNumber(final Function<N, T> asT) {this.asT = asT;}

    @Override
    public final N convertToDatabaseColumn(T attribute) { return attribute.get(); }

    @Override
    public final T convertToEntityAttribute(N value) { return asT.apply(value); }
  }

  abstract class FromBoolean<T extends Supplier<Boolean>> implements AttributeConverter<T, Boolean> {
    private final Function<Boolean, T> asT;

    protected FromBoolean(final Function<Boolean, T> asT) {this.asT = asT;}

    @Override
    public final Boolean convertToDatabaseColumn(T attribute) { return attribute.get(); }

    @Override
    public final T convertToEntityAttribute(Boolean value) { return asT.apply(value); }
  }

  @Converter
  final class AsUUID implements AttributeConverter<UUID, String> {
    @Override
    public String convertToDatabaseColumn(final UUID value) { return value.toString(); }

    @Override
    public UUID convertToEntityAttribute(final String value) { return UUID.fromString(value); }
  }
}
