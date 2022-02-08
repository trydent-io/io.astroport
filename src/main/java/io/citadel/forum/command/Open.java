package io.citadel.forum.command;

import io.citadel.domain.message.Command;

public record Open(String title) implements Command {
}
