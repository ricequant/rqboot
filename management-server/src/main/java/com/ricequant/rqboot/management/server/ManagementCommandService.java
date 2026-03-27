package com.ricequant.rqboot.management.server;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class ManagementCommandService {

  private final RQProcessInfo processInfo;

  private final ManagementControl control;

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

  public Map<String, Object> handleAction(String action, Map<String, Object> params) {
    String normalized = action == null ? "" : action.trim();
    Map<String, Object> safeParams = params == null ? Map.of() : params;
    return switch (normalized) {
      case "info" -> infoPayload();
      case "state" -> statePayload();
      case "setLogLevel" -> changeLogLevel(asString(safeParams.get("level")));
      case "shutdown" -> shutdownPayload(asString(safeParams.get("reason")));
      default -> throw new IllegalArgumentException("Unknown management action: " + normalized);
    };
  }

  private static String asString(Object value) {
    return value == null ? null : String.valueOf(value);
  }
}
