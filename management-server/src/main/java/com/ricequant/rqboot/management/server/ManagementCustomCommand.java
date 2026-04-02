package com.ricequant.rqboot.management.server;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public record ManagementCustomCommand(String name, String title, String description, List<ManagementCommandArg> args) {

  private static final Pattern NAME_PATTERN = Pattern.compile("[A-Za-z0-9_.-]+");

  public ManagementCustomCommand {
    name = normalizeName(name);
    title = normalizeText(title, name);
    description = normalizeText(description, null);
    args = List.copyOf(args == null ? List.of() : args);
    validateArgs(name, args);
  }

  public static ManagementCustomCommand of(String name) {
    return new ManagementCustomCommand(name, null, null, List.of());
  }

  public ManagementCustomCommand titled(String value) {
    return new ManagementCustomCommand(name, value, description, args);
  }

  public ManagementCustomCommand describedAs(String value) {
    return new ManagementCustomCommand(name, title, value, args);
  }

  public ManagementCustomCommand withArgs(ManagementCommandArg... values) {
    return new ManagementCustomCommand(name, title, description, List.of(values));
  }

  public Map<String, Object> toPayload() {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("name", name);
    payload.put("title", title);
    payload.put("description", description);
    payload.put("args", args.stream().map(ManagementCommandArg::toPayload).toList());
    return payload;
  }

  private static void validateArgs(String commandName, List<ManagementCommandArg> args) {
    Set<String> names = new HashSet<>();
    for (ManagementCommandArg arg : args) {
      Objects.requireNonNull(arg, "args");
      if (!names.add(arg.name())) {
        throw new IllegalArgumentException("Duplicate argument <" + arg.name() + "> for command <" + commandName + ">");
      }
    }
  }

  private static String normalizeName(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Management command name is required");
    }
    String normalized = value.trim();
    if (!NAME_PATTERN.matcher(normalized).matches()) {
      throw new IllegalArgumentException("Management command name must match " + NAME_PATTERN.pattern());
    }
    return normalized;
  }

  private static String normalizeText(String value, String fallback) {
    if (value == null || value.isBlank()) {
      return fallback;
    }
    return value.trim();
  }
}
