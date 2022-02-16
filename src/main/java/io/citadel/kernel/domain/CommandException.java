package io.citadel.kernel.domain;

public final class CommandException extends IllegalStateException {
  public CommandException(final Domain.Command command, final String aggregate, final String state) {
    super("Can't apply command %s when aggregate %s is on state %s".formatted(command.getClass().getSimpleName(), aggregate, state));
  }
}
