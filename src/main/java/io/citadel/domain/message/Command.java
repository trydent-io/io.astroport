package io.citadel.domain.message;

public interface Command {
  Event[] asEvents();

  default Event asEvent() {
    var events = asEvents();
    return events != null && events.length > 0 ? events[0] : throwIllegalStateException();
  }

  private Event throwIllegalStateException() {
    throw new IllegalStateException("Can't convert command to event");
  }
}
