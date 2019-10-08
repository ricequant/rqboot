package com.ricequant.rqboot.jmx.server;

/**
 * @author chenfeng
 */
public interface IJmxBeanRegistry {


  void registerDirectBean(Object mBean) throws JmxServerException;

  String register(Object object) throws JmxServerException;

  void deregister(String name) throws JmxServerException;

  boolean isRegistered(String name) throws JmxServerException;

  void withdrawService();
}
