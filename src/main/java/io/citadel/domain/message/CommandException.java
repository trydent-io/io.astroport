package io.citadel.domain.message;

public final class CommandException extends IllegalStateException {
  public CommandException(final Command command, final String aggregate, final String state) {
    super("Can't apply command %s when aggregate %s is on state %s".formatted(command.getClass().getSimpleName(), aggregate, state));
  }
}
