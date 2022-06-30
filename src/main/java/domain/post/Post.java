package domain.post;

import java.util.UUID;

public record Post(Title title) {
  record ID(UUID value) {}
  record Title(String value) {}
}
