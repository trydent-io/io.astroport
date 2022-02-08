package io.citadel.forum;

import io.citadel.domain.message.Command;
import io.citadel.forum.command.Commands;
import io.citadel.forum.state.States;

import java.util.function.Function;

public interface Forum extends Function<Command, Forum> {
  Commands commands = Commands.Defaults;
  States states = States.Defaults;
}
