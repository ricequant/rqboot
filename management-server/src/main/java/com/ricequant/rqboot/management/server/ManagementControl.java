package com.ricequant.rqboot.management.server;

public interface ManagementControl {

  void changeLogLevel(String level);

  void gracefulShutdown(String reason);
}
