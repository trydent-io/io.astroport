package io.citadel.kernel.eventstore;

import io.citadel.kernel.domain.Attribute;
import io.citadel.kernel.domain.Domain;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.eclipse.persistence.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Consumer;

@Entity(name = "EventLog")
@Table(name = "event_logs")
@UuidGenerator(name = Entry.ID)
public non-sealed class Entry implements EventLog {
  static final String ID = "EventLogId";

  @Id
  @GeneratedValue(generator = Entry.ID)
  @Convert(converter = Attribute.AsUUID.class)
  public UUID id;

  @Embedded
  public EventInfo event;
  @Embedded
  public AggregateInfo aggregate;

  public LocalDateTime persistedAt;

  public static Entry with(Consumer<Entry> consumer) {
    final var entry = new Entry();
    consumer.accept(entry);
    return entry;
  }

  @Override
  public Domain.Event<?> asEvent() {
    return Domain.Event.defaults.from(event.name, event.data).orElseThrow(() -> new IllegalStateException("Can't find factory method for event %s".formatted(event.name)));
  }
}
