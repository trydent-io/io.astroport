package io.astroport.flight;

import io.astroport.kernel.domain.UUIDAttribute;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import org.eclipse.persistence.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@UuidGenerator(name = "FlightId")
public class Flight {
  @Id
  @GeneratedValue(generator = "FlightId")
  @Convert(converter = UUIDAttribute.class)
  public UUID id;

  public String number;
  public LocalDateTime departure;
  public LocalDateTime arrival;
}
