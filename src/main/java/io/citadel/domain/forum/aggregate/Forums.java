package io.citadel.domain.forum.aggregate;

import io.citadel.domain.forum.Forum;
import io.vertx.core.Future;

import java.util.UUID;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

public sealed interface Forums permits Repository {

  Future<Forum.Aggregate> forum(Forum.ID id);

  Future<Forum.Aggregate> forum(Forum.ID id, Forum.Name name);
}
