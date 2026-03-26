package com.ricequant.rqboot.management.server;

import com.ricequant.rqboot.logging.LogConfiguration;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ManagementServer {

  private static final String TOKEN_HEADER = "X-Management-Token";

  private final ManagementServerConfig iConfig;

  private final ManagementInfoProvider iInfoProvider;

  private final ManagementStateProvider iStateProvider;

  private final ManagementControl iControl;

  private HttpServer iHttpServer;

  private ExecutorService iExecutorService;

  public ManagementServer(ManagementServerConfig config, ManagementInfoProvider infoProvider,
          ManagementStateProvider stateProvider, ManagementControl control) {
    iConfig = Objects.requireNonNull(config, "config");
    iInfoProvider = Objects.requireNonNull(infoProvider, "infoProvider");
    iStateProvider = Objects.requireNonNull(stateProvider, "stateProvider");
    iControl = Objects.requireNonNull(control, "control");
  }

  public synchronized void start() throws IOException {
    if (iHttpServer != null) {
      return;
    }

    InetSocketAddress address = new InetSocketAddress(iConfig.host(), iConfig.port());
    iHttpServer = HttpServer.create(address, 0);
    iExecutorService = Executors.newCachedThreadPool();
    iHttpServer.setExecutor(iExecutorService);
    iHttpServer.createContext("/management/info", new JsonHandler("GET") {
      @Override
      protected Object handleJson(HttpExchange exchange) {
        return buildInfoPayload();
      }
    });
    iHttpServer.createContext("/management/state", new JsonHandler("GET") {
      @Override
      protected Object handleJson(HttpExchange exchange) {
        return buildStatePayload();
      }
    });
    iHttpServer.createContext("/management/log-level", new JsonHandler("POST") {
      @Override
      protected Object handleJson(HttpExchange exchange) throws IOException {
        Map<String, Object> body = readJsonObject(exchange);
        Object level = body.get("level");
        if (!(level instanceof String levelString) || levelString.isBlank()) {
          throw new IllegalArgumentException("Field <level> is required");
        }

        iControl.changeLogLevel(levelString);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("ok", true);
        payload.put("debugLevel", iStateProvider.debugLevel());
        payload.put("updatedAt", Instant.now().toString());
        return payload;
      }
    });
    iHttpServer.createContext("/management/shutdown", new JsonHandler("POST") {
      @Override
      protected Object handleJson(HttpExchange exchange) throws IOException {
        Map<String, Object> body = readJsonObject(exchange);
        Object reason = body.get("reason");
        String shutdownReason = reason instanceof String ? (String) reason : "requested via management api";

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("ok", true);
        payload.put("requestedAt", Instant.now().toString());
        payload.put("reason", shutdownReason);

        new Thread(() -> iControl.gracefulShutdown(shutdownReason), "rqboot-management-shutdown").start();
        return payload;
      }
    });
    iHttpServer.start();

    System.out.println("Management server started at: " + getBoundAddress().getHostString() + ":" + getBoundPort());
  }

  public synchronized void stop() {
    if (iHttpServer != null) {
      iHttpServer.stop(0);
      iHttpServer = null;
    }

    if (iExecutorService != null) {
      iExecutorService.shutdownNow();
      iExecutorService = null;
    }
  }

  public synchronized int getBoundPort() {
    if (iHttpServer == null) {
      return iConfig.port();
    }
    return iHttpServer.getAddress().getPort();
  }

  public synchronized InetSocketAddress getBoundAddress() {
    if (iHttpServer == null) {
      return new InetSocketAddress(iConfig.host(), iConfig.port());
    }
    return iHttpServer.getAddress();
  }

  public static Map<String, Object> collectJvmState() {
    MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

    Map<String, Object> jvm = new LinkedHashMap<>();
    MemoryUsage heap = memoryBean.getHeapMemoryUsage();
    MemoryUsage nonHeap = memoryBean.getNonHeapMemoryUsage();
    jvm.put("heapUsedMb", toMb(heap.getUsed()));
    jvm.put("heapCommittedMb", toMb(heap.getCommitted()));
    jvm.put("heapMaxMb", toMb(heap.getMax()));
    jvm.put("nonHeapUsedMb", toMb(nonHeap.getUsed()));
    jvm.put("threadCount", threadBean.getThreadCount());
    jvm.put("processCpuLoad", cpuMetric(osBean, "getProcessCpuLoad"));
    jvm.put("systemCpuLoad", cpuMetric(osBean, "getSystemCpuLoad"));
    return jvm;
  }

  public static Map<String, Object> collectJvmInfo() {
    Map<String, Object> info = new LinkedHashMap<>();
    info.put("version", System.getProperty("java.version"));
    info.put("vendor", System.getProperty("java.vendor"));
    info.put("vmName", System.getProperty("java.vm.name"));
    return info;
  }

  public static String resolveHostName() {
    try {
      return InetAddress.getLocalHost().getHostAddress();
    }
    catch (Exception e) {
      return "unknown";
    }
  }

  public static long currentPid() {
    return ProcessHandle.current().pid();
  }

  private Map<String, Object> buildInfoPayload() {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("processName", iInfoProvider.processName());
    payload.put("applicationName", iInfoProvider.applicationName());
    payload.put("instanceName", iInfoProvider.instanceName());
    payload.put("pid", iInfoProvider.pid());
    payload.put("host", iInfoProvider.host());
    payload.put("startedAtEpochMs", iInfoProvider.startedAtEpochMs());
    payload.put("management", endpointsJson(iInfoProvider.endpoints()));
    payload.put("jvm", iInfoProvider.jvmInfo());

    Map<String, Object> build = new LinkedHashMap<>();
    build.put("libraries", iInfoProvider.buildLibraries());
    payload.put("build", build);
    return payload;
  }

  private Map<String, Object> buildStatePayload() {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("state", iStateProvider.lifecycleState().name());
    payload.put("healthy", iStateProvider.healthy());
    payload.put("uptimeMs", iStateProvider.uptimeMs());

    Map<String, Object> logging = new LinkedHashMap<>();
    logging.put("debugLevel", iStateProvider.debugLevel());
    payload.put("logging", logging);
    payload.put("jvm", iStateProvider.jvmState());
    return payload;
  }

  private Map<String, Object> endpointsJson(ManagementEndpointsInfo endpointsInfo) {
    Map<String, Object> management = new LinkedHashMap<>();

    Map<String, Object> http = new LinkedHashMap<>();
    http.put("enabled", endpointsInfo.httpEnabled());
    http.put("bindHost", endpointsInfo.httpHost());
    http.put("port", endpointsInfo.httpPort());
    management.put("http", http);

    Map<String, Object> jmx = new LinkedHashMap<>();
    jmx.put("enabled", endpointsInfo.jmxEnabled());
    jmx.put("host", endpointsInfo.jmxHost());
    jmx.put("port", endpointsInfo.jmxPort());
    management.put("jmx", jmx);
    return management;
  }

  private static double toMb(long bytes) {
    if (bytes < 0) {
      return -1;
    }
    return bytes / 1024.0 / 1024.0;
  }

  private static Double cpuMetric(OperatingSystemMXBean osBean, String methodName) {
    try {
      Object value = osBean.getClass().getMethod(methodName).invoke(osBean);
      if (value instanceof Number number) {
        double metric = number.doubleValue();
        if (metric >= 0) {
          return metric;
        }
      }
    }
    catch (Exception e) {
      return null;
    }
    return null;
  }

  private abstract class JsonHandler implements HttpHandler {

    private final String iMethod;

    private JsonHandler(String method) {
      iMethod = method;
    }

    @Override
    public final void handle(HttpExchange exchange) throws IOException {
      try {
        if (!iMethod.equals(exchange.getRequestMethod())) {
          writeJson(exchange, 405, Map.of("error", "method not allowed"));
          return;
        }

        if (!isAuthorized(exchange.getRequestHeaders())) {
          writeJson(exchange, 401, Map.of("error", "unauthorized"));
          return;
        }

        Object payload = handleJson(exchange);
        writeJson(exchange, 200, payload);
      }
      catch (IllegalArgumentException e) {
        writeJson(exchange, 400, Map.of("error", e.getMessage()));
      }
      catch (Exception e) {
        writeJson(exchange, 500, Map.of("error", e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()));
      }
      finally {
        exchange.close();
      }
    }

    protected abstract Object handleJson(HttpExchange exchange) throws Exception;
  }

  private boolean isAuthorized(Headers headers) {
    String actual = headers.getFirst(TOKEN_HEADER);
    return iConfig.token().equals(actual);
  }

  private Map<String, Object> readJsonObject(HttpExchange exchange) throws IOException {
    String body;
    try (InputStream inputStream = exchange.getRequestBody()) {
      body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8).trim();
    }

    if (body.isEmpty()) {
      return new LinkedHashMap<>();
    }

    return parseSimpleJsonObject(body);
  }

  private static Map<String, Object> parseSimpleJsonObject(String json) {
    String trimmed = json.trim();
    if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
      throw new IllegalArgumentException("Expected a JSON object body");
    }

    String content = trimmed.substring(1, trimmed.length() - 1).trim();
    Map<String, Object> ret = new LinkedHashMap<>();
    if (content.isEmpty()) {
      return ret;
    }

    List<String> parts = splitTopLevel(content);
    for (String part : parts) {
      int separator = part.indexOf(':');
      if (separator <= 0) {
        throw new IllegalArgumentException("Invalid JSON field: " + part);
      }
      String key = unquote(part.substring(0, separator).trim());
      String valueText = part.substring(separator + 1).trim();
      ret.put(key, parseSimpleValue(valueText));
    }
    return ret;
  }

  private static List<String> splitTopLevel(String content) {
    List<String> parts = new ArrayList<>();
    StringBuilder current = new StringBuilder();
    boolean inString = false;
    boolean escaped = false;

    for (int i = 0; i < content.length(); i++) {
      char c = content.charAt(i);
      if (escaped) {
        current.append(c);
        escaped = false;
        continue;
      }
      if (c == '\\') {
        current.append(c);
        escaped = true;
        continue;
      }
      if (c == '"') {
        inString = !inString;
        current.append(c);
        continue;
      }
      if (c == ',' && !inString) {
        parts.add(current.toString().trim());
        current.setLength(0);
        continue;
      }
      current.append(c);
    }
    parts.add(current.toString().trim());
    return parts;
  }

  private static Object parseSimpleValue(String valueText) {
    if (valueText.startsWith("\"") && valueText.endsWith("\"")) {
      return unquote(valueText);
    }

    String lower = valueText.toLowerCase(Locale.ROOT);
    if ("true".equals(lower)) {
      return Boolean.TRUE;
    }
    if ("false".equals(lower)) {
      return Boolean.FALSE;
    }
    if ("null".equals(lower)) {
      return null;
    }

    try {
      if (valueText.contains(".")) {
        return Double.parseDouble(valueText);
      }
      return Long.parseLong(valueText);
    }
    catch (NumberFormatException e) {
      throw new IllegalArgumentException("Unsupported JSON value: " + valueText);
    }
  }

  private static String unquote(String text) {
    String trimmed = text.trim();
    if (trimmed.length() >= 2 && trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
      trimmed = trimmed.substring(1, trimmed.length() - 1);
    }
    return trimmed.replace("\\\"", "\"").replace("\\\\", "\\");
  }

  private void writeJson(HttpExchange exchange, int status, Object payload) throws IOException {
    byte[] bytes = toJson(payload).getBytes(StandardCharsets.UTF_8);
    Headers headers = exchange.getResponseHeaders();
    headers.set("Content-Type", "application/json; charset=utf-8");
    exchange.sendResponseHeaders(status, bytes.length);
    try (OutputStream outputStream = exchange.getResponseBody()) {
      outputStream.write(bytes);
    }
  }

  private static String toJson(Object value) {
    if (value == null) {
      return "null";
    }
    if (value instanceof String string) {
      return '"' + escapeJson(string) + '"';
    }
    if (value instanceof Number || value instanceof Boolean) {
      return value.toString();
    }
    if (value instanceof Map<?, ?> map) {
      StringBuilder builder = new StringBuilder("{");
      boolean first = true;
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        if (!first) {
          builder.append(',');
        }
        first = false;
        builder.append(toJson(String.valueOf(entry.getKey()))).append(':').append(toJson(entry.getValue()));
      }
      return builder.append('}').toString();
    }
    if (value instanceof Iterable<?> iterable) {
      StringBuilder builder = new StringBuilder("[");
      boolean first = true;
      for (Object item : iterable) {
        if (!first) {
          builder.append(',');
        }
        first = false;
        builder.append(toJson(item));
      }
      return builder.append(']').toString();
    }
    throw new IllegalArgumentException("Unsupported json type: " + value.getClass());
  }

  private static String escapeJson(String value) {
    return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
            .replace("\r", "\\r");
  }
}
