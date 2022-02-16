package io.citadel.member;

import io.citadel.kernel.domain.Attribute;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import org.eclipse.persistence.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@UuidGenerator(name = Member.ID)
public class Member {
  static final String ID = "MemberId";

  @Id
  @GeneratedValue(generator = Member.ID)
  @Convert(converter = Attribute.AsUUID.class)
  public UUID id;

  public String firstName;
  public String lastName;
}
