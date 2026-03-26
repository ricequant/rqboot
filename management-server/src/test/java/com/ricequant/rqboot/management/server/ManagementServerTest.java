package com.ricequant.rqboot.management.server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
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
    assertTrue(info.body().contains("\"processName\":\"demo-process\""));
    assertTrue(info.body().contains("\"management\""));

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

  private ManagementServer newServer(AtomicReference<String> level, AtomicBoolean shutdown) {
    ManagementInfoProvider infoProvider = new ManagementInfoProvider() {
      @Override
      public String processName() {
        return "demo-process";
      }

      @Override
      public String applicationName() {
        return "demo-app";
      }

      @Override
      public String instanceName() {
        return "demo-instance";
      }

      @Override
      public long startedAtEpochMs() {
        return 123456789L;
      }

      @Override
      public long pid() {
        return 42L;
      }

      @Override
      public String host() {
        return "127.0.0.1";
      }

      @Override
      public Map<String, Object> jvmInfo() {
        return Map.of("version", "21", "vendor", "RiceQuant", "vmName", "TestVM");
      }

      @Override
      public List<Map<String, Object>> buildLibraries() {
        return List.of(Map.of("name", "boot", "version", "1.0-SNAPSHOT"));
      }

      @Override
      public ManagementEndpointsInfo endpoints() {
        return new ManagementEndpointsInfo(true, "0.0.0.0", iServer == null ? 0 : iServer.getBoundPort(), true,
                "127.0.0.1", 9999);
      }
    };

    ManagementStateProvider stateProvider = new ManagementStateProvider() {
      @Override
      public ManagementLifecycleState lifecycleState() {
        return ManagementLifecycleState.RUNNING;
      }

      @Override
      public boolean healthy() {
        return true;
      }

      @Override
      public long uptimeMs() {
        return 1000L;
      }

      @Override
      public String debugLevel() {
        return level.get();
      }

      @Override
      public Map<String, Object> jvmState() {
        return Map.of("heapUsedMb", 10.0, "threadCount", 5);
      }
    };

    ManagementControl control = new ManagementControl() {
      @Override
      public void changeLogLevel(String newLevel) {
        level.set(newLevel);
      }

      @Override
      public void gracefulShutdown(String reason) {
        shutdown.set(true);
      }
    };

    return new ManagementServer(new ManagementServerConfig("127.0.0.1", 0, "token"), infoProvider, stateProvider,
            control);
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
