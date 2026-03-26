package com.ricequant.rqboot.management.server;

import java.util.Map;

public interface ManagementStateProvider {

  ManagementLifecycleState lifecycleState();

  boolean healthy();

  long uptimeMs();

  String debugLevel();

  Map<String, Object> jvmState();
}
