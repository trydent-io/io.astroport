package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.vertx.core.Future;

public interface Forums {

  Future<Forum.Transaction> forum(Forum.ID id);

  Future<Forum.Transaction> forum(Forum.ID id, Forum.Name name);
}
