package com.ricequant.rqboot.management.server;

import com.alibaba.fastjson.JSON;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ManagementServer {

  private static final String TOKEN_HEADER = "X-Management-Token";

  private final ManagementServerConfig iConfig;

  private final ManagementCommandService iCommandService;

  private HttpServer iHttpServer;

  private ExecutorService iExecutorService;

  public ManagementServer(ManagementServerConfig config, ManagementCommandService commandService) {
    iConfig = Objects.requireNonNull(config, "config");
    iCommandService = Objects.requireNonNull(commandService, "commandService");
  }

  public synchronized void start() throws IOException {
    if (iHttpServer != null) {
      return;
    }

    HttpServer server = createHttpServer();
    ExecutorService executorService = Executors.newCachedThreadPool();
    server.setExecutor(executorService);
    server.createContext("/management/info", new JsonHandler("GET") {
      @Override
      protected Object handleJson(HttpExchange exchange) {
        return iCommandService.infoPayload();
      }
    });
    server.createContext("/management/state", new JsonHandler("GET") {
      @Override
      protected Object handleJson(HttpExchange exchange) {
        return iCommandService.statePayload();
      }
    });
    server.createContext("/management/log-level", new JsonHandler("POST") {
      @Override
      protected Object handleJson(HttpExchange exchange) throws IOException {
        Map<String, Object> body = readJsonObject(exchange);
        return iCommandService.changeLogLevel(stringValue(body.get("level")));
      }
    });
    server.createContext("/management/shutdown", new JsonHandler("POST") {
      @Override
      protected Object handleJson(HttpExchange exchange) throws IOException {
        Map<String, Object> body = readJsonObject(exchange);
        String shutdownReason = stringValue(body.get("reason"));
        Map<String, Object> payload = iCommandService.shutdownPayload(shutdownReason);
        new Thread(() -> iCommandService.gracefulShutdown(shutdownReason), "rqboot-management-shutdown").start();
        return payload;
      }
    });
    server.createContext("/management/commands", new CustomCommandHandler());
    server.start();

    iHttpServer = server;
    iExecutorService = executorService;

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

  public ManagementCommandService commandService() {
    return iCommandService;
  }

  private HttpServer createHttpServer() throws IOException {
    InetSocketAddress requestedAddress = new InetSocketAddress(iConfig.host(), iConfig.port());
    try {
      return HttpServer.create(requestedAddress, 0);
    }
    catch (java.net.BindException e) {
      if (iConfig.port() == 0) {
        throw e;
      }

      System.err.println("Management port " + iConfig.port() + " is unavailable on " + iConfig.host()
              + ", falling back to a random port.");
      return HttpServer.create(new InetSocketAddress(iConfig.host(), 0), 0);
    }
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

  private class CustomCommandHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
      try {
        if (!isAuthorized(exchange.getRequestHeaders())) {
          writeJson(exchange, 401, Map.of("error", "unauthorized"));
          return;
        }

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        if ("GET".equals(method) && "/management/commands".equals(path)) {
          writeJson(exchange, 200, iCommandService.customCommandsPayload());
          return;
        }

        if ("POST".equals(method) && path.startsWith("/management/commands/")) {
          String commandName = URLDecoder.decode(path.substring("/management/commands/".length()), StandardCharsets.UTF_8);
          writeJson(exchange, 200, iCommandService.executeCustomCommand(commandName, readJsonObject(exchange)));
          return;
        }

        if ("GET".equals(method) || "POST".equals(method)) {
          writeJson(exchange, 404, Map.of("error", "not found"));
          return;
        }

        writeJson(exchange, 405, Map.of("error", "method not allowed"));
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
      return Map.of();
    }

    Object parsed;
    try {
      parsed = JSON.parse(body);
    }
    catch (Exception e) {
      throw new IllegalArgumentException("Expected a JSON object body");
    }
    if (!(parsed instanceof Map<?, ?> rawMap)) {
      throw new IllegalArgumentException("Expected a JSON object body");
    }

    Map<String, Object> ret = new LinkedHashMap<>();
    for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
      ret.put(String.valueOf(entry.getKey()), entry.getValue());
    }
    return ret;
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
    return JSON.toJSONString(value);
  }

  private static String stringValue(Object value) {
    return value == null ? null : String.valueOf(value);
  }
}
