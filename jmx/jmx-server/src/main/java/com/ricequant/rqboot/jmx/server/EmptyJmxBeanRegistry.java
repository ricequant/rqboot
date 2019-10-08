package com.ricequant.rqboot.jmx.server;

/**
 * @author chenfeng
 */
public class EmptyJmxBeanRegistry implements IJmxBeanRegistry {

  @Override
  public void registerDirectBean(Object mBean) throws JmxServerException {
    register(mBean);
  }

  @Override
  public String register(Object object) throws JmxServerException {
    System.out.println("Trying to register bean: " + object.getClass().getCanonicalName() + " but bean server is not "
            + "started ");
    return object.getClass().getSimpleName();
  }

  @Override
  public void deregister(String name) throws JmxServerException {

  }

  @Override
  public boolean isRegistered(String name) throws JmxServerException {
    return false;
  }

  @Override
  public void withdrawService() {

  }
}
