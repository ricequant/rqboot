package com.ricequant.rqboot.management.server;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RQProcessInfo {

  private final String processName;

  private final String applicationName;

  private final String instanceName;

  private final long startedAtEpochMs;

  private final long pid;

  private final String host;

  private volatile boolean managementEnabled;

  private volatile String managementHost;

  private volatile int managementPort;

  private volatile boolean jmxEnabled;

  private volatile String jmxHost;

  private volatile Integer jmxPort;

  private volatile String debugLevel;

  private volatile ManagementLifecycleState lifecycleState;

  private volatile List<Map<String, Object>> buildLibraries;

  public RQProcessInfo(String processName, String applicationName, String instanceName, long startedAtEpochMs, long pid,
          String host) {
    this.processName = processName;
    this.applicationName = applicationName;
    this.instanceName = instanceName;
    this.startedAtEpochMs = startedAtEpochMs;
    this.pid = pid;
    this.host = host;
    this.debugLevel = "0";
    this.lifecycleState = ManagementLifecycleState.STARTING;
    this.buildLibraries = List.of();
  }

  public RQProcessInfo(RQProcessInfo other) {
    this.processName = other.processName;
    this.applicationName = other.applicationName;
    this.instanceName = other.instanceName;
    this.startedAtEpochMs = other.startedAtEpochMs;
    this.pid = other.pid;
    this.host = other.host;
    this.managementEnabled = other.managementEnabled;
    this.managementHost = other.managementHost;
    this.managementPort = other.managementPort;
    this.jmxEnabled = other.jmxEnabled;
    this.jmxHost = other.jmxHost;
    this.jmxPort = other.jmxPort;
    this.debugLevel = other.debugLevel;
    this.lifecycleState = other.lifecycleState;
    this.buildLibraries = new ArrayList<>(other.buildLibraries);
  }

  public RQProcessInfo copy() {
    return new RQProcessInfo(this);
  }

  public RQProcessInfo debugLevel(String value) {
    this.debugLevel = value;
    return this;
  }

  public RQProcessInfo lifecycleState(ManagementLifecycleState value) {
    this.lifecycleState = value;
    return this;
  }

  public RQProcessInfo managementEndpoint(String host, int port) {
    this.managementEnabled = true;
    this.managementHost = host;
    this.managementPort = port;
    return this;
  }

  public RQProcessInfo jmxEndpoint(String host, Integer port) {
    this.jmxEnabled = true;
    this.jmxHost = host;
    this.jmxPort = port;
    return this;
  }

  public RQProcessInfo buildLibraries(List<Map<String, Object>> value) {
    this.buildLibraries = value == null ? List.of() : new ArrayList<>(value);
    return this;
  }

  public String processName() {
    return processName;
  }

  public String debugLevel() {
    return debugLevel;
  }

  public ManagementLifecycleState lifecycleState() {
    return lifecycleState;
  }

  public Map<String, Object> infoPayload() {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("producerType", "rqboot");
    payload.put("title", applicationName);
    payload.put("subtitle", instanceName);
    payload.put("tile", List.of(infoItem("pid", "PID", "number", pid)));
    payload.put("overview", List.of(infoItem("applicationName", "Application", "string", applicationName),
            infoItem("instanceName", "Instance", "string", instanceName), infoItem("pid", "PID", "number", pid),
            infoItem("host", "Host", "string", host), infoItem("startedAt", "Started At", "timestamp", startedAtEpochMs)));
    payload.put("sections", List.of(fieldsSection("identity", "Identity",
                    List.of(infoItem("processName", "Process Name", "string", processName),
                            infoItem("applicationName", "Application Name", "string", applicationName),
                            infoItem("instanceName", "Instance Name", "string", instanceName),
                            infoItem("pid", "PID", "number", pid), infoItem("host", "Host", "string", host),
                            infoItem("startedAt", "Started At", "timestamp", startedAtEpochMs))),
            fieldsSection("management", "Management", List.of(infoItem("httpEnabled", "HTTP Enabled", "boolean", managementEnabled),
                    infoItem("httpBindHost", "HTTP Bind Host", "string", managementHost),
                    infoItem("httpPort", "HTTP Port", "number", managementPort),
                    infoItem("jmxEnabled", "JMX Enabled", "boolean", jmxEnabled),
                    infoItem("jmxHost", "JMX Host", "string", jmxHost), infoItem("jmxPort", "JMX Port", "number", jmxPort))),
            fieldsSection("jvm", "JVM", List.of(infoItem("version", "Version", "string", System.getProperty("java.version")),
                    infoItem("vendor", "Vendor", "string", System.getProperty("java.vendor")),
                    infoItem("vmName", "VM Name", "string", System.getProperty("java.vm.name")))),
            jsonSection("build", "Build", Map.of("libraries", buildLibraries))));
    return payload;
  }

  public Map<String, Object> statePayload() {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("state", lifecycleState.name());
    payload.put("healthy", lifecycleState == ManagementLifecycleState.RUNNING);
    payload.put("uptimeMs", Math.max(0, System.currentTimeMillis() - startedAtEpochMs));

    Map<String, Object> logging = new LinkedHashMap<>();
    logging.put("debugLevel", debugLevel);
    payload.put("logging", logging);
    payload.put("jvm", collectJvmState());
    return payload;
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

  private static Map<String, Object> infoItem(String key, String label, String type, Object value) {
    Map<String, Object> item = new LinkedHashMap<>();
    item.put("key", key);
    item.put("label", label);
    item.put("type", type);
    item.put("value", value);
    return item;
  }

  private static Map<String, Object> fieldsSection(String id, String title, List<Map<String, Object>> items) {
    Map<String, Object> section = new LinkedHashMap<>();
    section.put("id", id);
    section.put("title", title);
    section.put("kind", "fields");
    section.put("items", items);
    return section;
  }

  private static Map<String, Object> jsonSection(String id, String title, Object value) {
    Map<String, Object> section = new LinkedHashMap<>();
    section.put("id", id);
    section.put("title", title);
    section.put("kind", "json");
    section.put("value", value);
    return section;
  }
}
