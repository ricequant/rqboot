package com.ricequant.rqboot.management.server;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record ManagementCommandArg(String name, String title, String description, ManagementCommandArgType type,
        boolean required) {

  public ManagementCommandArg {
    Objects.requireNonNull(type, "type");
    name = normalizeName(name, "argument");
    title = normalizeText(title, name);
    description = normalizeText(description, null);
  }

  public static ManagementCommandArg required(String name, ManagementCommandArgType type) {
    return new ManagementCommandArg(name, null, null, type, true);
  }

  public static ManagementCommandArg optional(String name, ManagementCommandArgType type) {
    return new ManagementCommandArg(name, null, null, type, false);
  }

  public ManagementCommandArg titled(String value) {
    return new ManagementCommandArg(name, value, description, type, required);
  }

  public ManagementCommandArg describedAs(String value) {
    return new ManagementCommandArg(name, title, value, type, required);
  }

  public Map<String, Object> toPayload() {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("name", name);
    payload.put("title", title);
    payload.put("description", description);
    payload.put("type", type.id());
    payload.put("required", required);
    return payload;
  }

  private static String normalizeName(String value, String label) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Management command " + label + " name is required");
    }
    return value.trim();
  }

  private static String normalizeText(String value, String fallback) {
    if (value == null || value.isBlank()) {
      return fallback;
    }
    return value.trim();
  }
}
