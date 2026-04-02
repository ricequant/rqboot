package com.ricequant.rqboot.management.server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.ServerSocket;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManagementServerTest {

  private ManagementServer iServer;

  @AfterEach
  void tearDown() {
    if (iServer != null) {
      iServer.stop();
    }
  }

  @Test
  void requiresTokenForReadEndpoints() throws Exception {
    AtomicReference<String> level = new AtomicReference<>("0");
    AtomicBoolean shutdown = new AtomicBoolean(false);
    iServer = newServer(level, shutdown);
    iServer.start();

    HttpResponse<String> response = HttpClient.newHttpClient().send(HttpRequest.newBuilder(uri("/management/info"))
            .GET().build(), HttpResponse.BodyHandlers.ofString());

    assertEquals(401, response.statusCode());
  }

  @Test
  void exposesStaticAndDynamicEndpointsAndMutations() throws Exception {
    AtomicReference<String> level = new AtomicReference<>("1");
    AtomicBoolean shutdown = new AtomicBoolean(false);
    iServer = newServer(level, shutdown);
    iServer.start();

    HttpResponse<String> info = send("/management/info", "GET", null);
    assertEquals(200, info.statusCode());
    assertTrue(info.body().contains("\"producerType\":\"rqboot\""));
    assertTrue(info.body().contains("\"title\":\"demo-app\""));
    assertTrue(info.body().contains("\"overview\""));
    assertTrue(info.body().contains("\"sections\""));

    HttpResponse<String> state = send("/management/state", "GET", null);
    assertEquals(200, state.statusCode());
    assertTrue(state.body().contains("\"state\":\"RUNNING\""));
    assertTrue(state.body().contains("\"debugLevel\":\"1\""));

    HttpResponse<String> change = send("/management/log-level", "POST", "{\"level\":\"2\"}");
    assertEquals(200, change.statusCode());
    assertEquals("2", level.get());

    HttpResponse<String> shutdownResponse = send("/management/shutdown", "POST", "{\"reason\":\"test\"}");
    assertEquals(200, shutdownResponse.statusCode());

    for (int i = 0; i < 20 && !shutdown.get(); i++) {
      Thread.sleep(20);
    }
    assertTrue(shutdown.get());
  }

  @Test
  void exposesAndExecutesCustomCommands() throws Exception {
    AtomicReference<String> level = new AtomicReference<>("1");
    AtomicBoolean shutdown = new AtomicBoolean(false);
    iServer = newServer(level, shutdown);
    iServer.start();

    HttpResponse<String> listResponse = send("/management/commands", "GET", null);
    assertEquals(200, listResponse.statusCode());
    assertTrue(listResponse.body().contains("\"name\":\"sampleCommand\""));
    assertTrue(listResponse.body().contains("\"type\":\"long[]\""));
    assertTrue(listResponse.body().contains("\"type\":\"double[]\""));
    assertTrue(listResponse.body().contains("\"type\":\"string[]\""));

    HttpResponse<String> executeResponse = send("/management/commands/sampleCommand", "POST",
            "{\"count\":3,\"ratio\":1.5,\"labels\":[\"a\",\"b\"],\"values\":[1,2,3],\"weights\":[0.5,1.5]}");
    assertEquals(200, executeResponse.statusCode());
    assertTrue(executeResponse.body().contains("\"sum\":6"));
    assertTrue(executeResponse.body().contains("\"weighted\":2.0"));
    assertTrue(executeResponse.body().contains("\"labels\":[\"a\",\"b\"]"));
  }

  @Test
  void fallsBackToRandomPortWhenConfiguredPortIsOccupied() throws Exception {
    try (ServerSocket occupied = new ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"))) {
      AtomicReference<String> level = new AtomicReference<>("1");
      AtomicBoolean shutdown = new AtomicBoolean(false);
      iServer = newServer(level, shutdown, occupied.getLocalPort());
      iServer.start();

      assertTrue(iServer.getBoundPort() > 0);
      assertTrue(iServer.getBoundPort() != occupied.getLocalPort());

      HttpResponse<String> info = send("/management/info", "GET", null);
      assertEquals(200, info.statusCode());
    }
  }

  private ManagementServer newServer(AtomicReference<String> level, AtomicBoolean shutdown) {
    return newServer(level, shutdown, 0);
  }

  private ManagementServer newServer(AtomicReference<String> level, AtomicBoolean shutdown, int configuredPort) {
    RQProcessInfo processInfo = new RQProcessInfo("demo-process", "demo-app", "demo-instance", 123456789L, 42L,
            "127.0.0.1")
            .debugLevel(level.get())
            .lifecycleState(ManagementLifecycleState.RUNNING)
            .buildLibraries(List.of(Map.of("name", "boot", "version", "1.0-SNAPSHOT")))
            .managementEndpoint("127.0.0.1", 0)
            .jmxEndpoint("127.0.0.1", 9999);

    ManagementControl control = new ManagementControl() {
      @Override
      public void changeLogLevel(String newLevel) {
        level.set(newLevel);
        processInfo.debugLevel(newLevel);
      }

      @Override
      public void gracefulShutdown(String reason) {
        shutdown.set(true);
      }
    };

    ManagementCommandService commandService = new ManagementCommandService(processInfo, control);
    commandService.registerCustomCommand(new ManagementCustomCommand("sampleCommand", "Sample Command",
            "Exercises typed argument parsing", java.util.List.of(
                    ManagementCommandArg.required("count", ManagementCommandArgType.LONG),
                    ManagementCommandArg.required("ratio", ManagementCommandArgType.DOUBLE),
                    ManagementCommandArg.required("labels", ManagementCommandArgType.STRING_ARRAY),
                    ManagementCommandArg.required("values", ManagementCommandArgType.LONG_ARRAY),
                    ManagementCommandArg.required("weights", ManagementCommandArgType.DOUBLE_ARRAY))),
            args -> {
              long[] values = (long[]) args.get("values");
              double[] weights = (double[]) args.get("weights");
              long sum = 0L;
              for (long value : values) {
                sum += value;
              }

              double weighted = 0D;
              for (double weight : weights) {
                weighted += weight;
              }

              return Map.of("count", args.get("count"), "ratio", args.get("ratio"), "sum", sum, "weighted", weighted,
                      "labels", args.get("labels"));
            });

    return new ManagementServer(new ManagementServerConfig("127.0.0.1", configuredPort, "token"), commandService);
  }

  private HttpResponse<String> send(String path, String method, String body) throws IOException, InterruptedException {
    HttpRequest.Builder builder = HttpRequest.newBuilder(uri(path)).header("X-Management-Token", "token");
    if ("POST".equals(method)) {
      builder.header("Content-Type", "application/json");
      builder.POST(HttpRequest.BodyPublishers.ofString(body == null ? "" : body));
    }
    else {
      builder.GET();
    }
    return HttpClient.newHttpClient().send(builder.build(), HttpResponse.BodyHandlers.ofString());
  }

  private URI uri(String path) {
    return URI.create("http://127.0.0.1:" + iServer.getBoundPort() + path);
  }
}
