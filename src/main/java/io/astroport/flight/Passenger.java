package io.astroport.flight;

import io.astroport.kernel.domain.UUIDAttribute;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import org.eclipse.persistence.annotations.UuidGenerator;

import java.util.Objects;
import java.util.UUID;

@Entity
@UuidGenerator(name = Passenger.ID)
public class Passenger {
  static final String ID = "PassengerId";

  @Id
  @GeneratedValue(generator = Passenger.ID)
  @Convert(converter = UUIDAttribute.class)
  public UUID id;

  public String firstName;
  public String lastName;

  @Override
  public boolean equals(final Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    final var passenger = (Passenger) o;

    if (!id.equals(passenger.id))
      return false;
    if (!Objects.equals(firstName, passenger.firstName))
      return false;
    return Objects.equals(lastName, passenger.lastName);
  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
    result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
    return result;
  }
}
