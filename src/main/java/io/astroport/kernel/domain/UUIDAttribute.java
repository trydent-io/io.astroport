package io.astroport.kernel.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.UUID;
@Converter
public class UUIDAttribute implements AttributeConverter<UUID, String> {
  @Override
  public String convertToDatabaseColumn(final UUID value) {
    return value.toString();
  }

  @Override
  public UUID convertToEntityAttribute(final String value) {
    return UUID.fromString(value);
  }
}
