package io.citadel.forum.entity;

import io.citadel.domain.entity.Attribute;
import io.citadel.forum.member.Member;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import org.eclipse.persistence.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Consumer;

@Entity(name = "Forum")
@UuidGenerator(name = Root.ID)
public class Root {
  static final String ID = "ForumId";

  @Id
  @GeneratedValue(generator = Root.ID)
  @Convert(converter = Attribute.AsUUID.class)
  public UUID id;

  public String title;
  public String description;
  public LocalDateTime openedAt;
  public LocalDateTime closedAt;

  @ManyToOne
  public Member openedBy;

  @ManyToOne
  public Member closedBy;

  public static Root with(Consumer<Root> consumer) {
    final var root = new Root();
    consumer.accept(root);
    return root;
  }
}
