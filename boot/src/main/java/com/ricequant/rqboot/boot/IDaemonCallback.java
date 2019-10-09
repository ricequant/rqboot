package com.ricequant.rqboot.boot;

/**
 * @author chenfeng
 */
public interface IDaemonCallback {

  void init(String[] args) throws Exception;

  void start() throws Exception;

  void stop();

  void destroy();
}
