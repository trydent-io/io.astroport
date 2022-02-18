package io.citadel.topic;

import io.citadel.kernel.domain.Attribute.AsUUID;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import org.eclipse.persistence.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@UuidGenerator(name = Topic.ID)
public class Topic implements Type {
  public static final String ID = "TopicId";

  @Id
  @GeneratedValue(generator = Topic.ID)
  @Convert(converter = AsUUID.class)
  public UUID id;

  @Convert(converter = Attribute.AsTitle.class)
  public Title title;
  @Convert(converter = Attribute.AsText.class)
  public Text text;
}
