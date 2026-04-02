package com.ricequant.rqboot.management.server;

import java.util.Map;

@FunctionalInterface
public interface ManagementCustomCommandHandler {

  Map<String, Object> execute(Map<String, Object> args) throws Exception;
}
