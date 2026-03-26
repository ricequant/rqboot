package com.ricequant.rqboot.management.server;

import java.util.List;
import java.util.Map;

public interface ManagementInfoProvider {

  String processName();

  String applicationName();

  String instanceName();

  long startedAtEpochMs();

  long pid();

  String host();

  Map<String, Object> jvmInfo();

  List<Map<String, Object>> buildLibraries();

  ManagementEndpointsInfo endpoints();
}
