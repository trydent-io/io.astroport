package io.citadel.forum.command;

public enum Commands {
  Defaults;

  public static Open open(String title) { return new Open(title); }
}
