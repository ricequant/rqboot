package com.ricequant.rqboot.management.server;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ManagementCommandService {

  private final RQProcessInfo processInfo;

  private final ManagementControl control;

  private final Map<String, RegisteredCommand> customCommands = new LinkedHashMap<>();

  public ManagementCommandService(RQProcessInfo processInfo, ManagementControl control) {
    this.processInfo = Objects.requireNonNull(processInfo, "processInfo");
    this.control = Objects.requireNonNull(control, "control");
  }

  public Map<String, Object> infoPayload() {
    return processInfo.infoPayload();
  }

  public Map<String, Object> statePayload() {
    return processInfo.statePayload();
  }

  public Map<String, Object> changeLogLevel(String level) {
    if (level == null || level.isBlank()) {
      throw new IllegalArgumentException("Field <level> is required");
    }

    control.changeLogLevel(level);
    processInfo.debugLevel(level);

    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("ok", true);
    payload.put("debugLevel", processInfo.debugLevel());
    payload.put("updatedAt", Instant.now().toString());
    return payload;
  }

  public Map<String, Object> shutdownPayload(String reason) {
    String shutdownReason = reason == null || reason.isBlank() ? "requested via management api" : reason;
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("ok", true);
    payload.put("requestedAt", Instant.now().toString());
    payload.put("reason", shutdownReason);
    return payload;
  }

  public void gracefulShutdown(String reason) {
    String shutdownReason = reason == null || reason.isBlank() ? "requested via management api" : reason;
    control.gracefulShutdown(shutdownReason);
  }

  public void registerCustomCommand(ManagementCustomCommand command, ManagementCustomCommandHandler handler) {
    Objects.requireNonNull(command, "command");
    Objects.requireNonNull(handler, "handler");
    synchronized (customCommands) {
      if (customCommands.containsKey(command.name())) {
        throw new IllegalArgumentException("Management command already exists: " + command.name());
      }
      customCommands.put(command.name(), new RegisteredCommand(command, handler));
    }
  }

  public Map<String, Object> customCommandsPayload() {
    List<Map<String, Object>> items;
    synchronized (customCommands) {
      items = customCommands.values().stream().map(RegisteredCommand::command).map(ManagementCustomCommand::toPayload).toList();
    }

    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("items", items);
    return payload;
  }

  public Map<String, Object> executeCustomCommand(String commandName, Map<String, Object> params) throws Exception {
    RegisteredCommand registeredCommand;
    synchronized (customCommands) {
      registeredCommand = customCommands.get(commandName);
    }

    if (registeredCommand == null) {
      throw new IllegalArgumentException("Unknown management command: " + commandName);
    }

    Map<String, Object> convertedArgs = convertArgs(registeredCommand.command(), params);
    Map<String, Object> result = registeredCommand.handler().execute(Collections.unmodifiableMap(convertedArgs));
    return result == null ? Map.of() : new LinkedHashMap<>(result);
  }

  public Map<String, Object> handleAction(String action, Map<String, Object> params) throws Exception {
    String normalized = action == null ? "" : action.trim();
    Map<String, Object> safeParams = params == null ? Map.of() : params;
    return switch (normalized) {
      case "info" -> infoPayload();
      case "state" -> statePayload();
      case "setLogLevel" -> changeLogLevel(asString(safeParams.get("level")));
      case "shutdown" -> shutdownPayload(asString(safeParams.get("reason")));
      case "listCommands" -> customCommandsPayload();
      case "runCommand" -> executeCustomCommand(asString(safeParams.get("name")), asMap(safeParams.get("args")));
      default -> throw new IllegalArgumentException("Unknown management action: " + normalized);
    };
  }

  private Map<String, Object> convertArgs(ManagementCustomCommand command, Map<String, Object> params) {
    Map<String, Object> safeParams = params == null ? Map.of() : params;
    for (String key : safeParams.keySet()) {
      boolean known = command.args().stream().anyMatch(arg -> arg.name().equals(key));
      if (!known) {
        throw new IllegalArgumentException("Unknown argument <" + key + "> for command <" + command.name() + ">");
      }
    }

    Map<String, Object> converted = new LinkedHashMap<>();
    for (ManagementCommandArg arg : command.args()) {
      boolean present = safeParams.containsKey(arg.name());
      Object rawValue = safeParams.get(arg.name());
      if (!present || rawValue == null) {
        if (arg.required()) {
          throw new IllegalArgumentException("Argument <" + arg.name() + "> is required");
        }
        continue;
      }
      converted.put(arg.name(), arg.type().convert(rawValue, arg.name()));
    }
    return converted;
  }

  private static String asString(Object value) {
    return value == null ? null : String.valueOf(value);
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> asMap(Object value) {
    if (value == null) {
      return Map.of();
    }
    if (value instanceof Map<?, ?> rawMap) {
      Map<String, Object> ret = new LinkedHashMap<>();
      for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
        ret.put(String.valueOf(entry.getKey()), entry.getValue());
      }
      return ret;
    }
    throw new IllegalArgumentException("Expected an object for command args");
  }

  private record RegisteredCommand(ManagementCustomCommand command, ManagementCustomCommandHandler handler) {
  }
}
