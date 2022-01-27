package io.citadel.forum.topic;

import io.citadel.forum.topic.Attribute;
import io.citadel.forum.topic.Name;
import io.citadel.forum.topic.Type;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import org.eclipse.persistence.annotations.UuidGenerator;

import java.util.UUID;

import static io.citadel.domain.entity.Attribute.AsUUID;

@Entity
@UuidGenerator(name = Name.TopicId)
public class Topic implements Type {
  @Id
  @GeneratedValue(generator = Name.TopicId)
  @Convert(converter = AsUUID.class)
  public UUID id;

  @Convert(converter = Attribute.AsTitle.class)
  public Title title;
  @Convert(converter = Attribute.AsText.class)
  public Text text;
}
