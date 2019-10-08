package com.ricequant.rqboot.jmx.server;

import com.ricequant.rqboot.jmx.server.sample.BeanSample;
import com.ricequant.rqboot.jmx.server.sample.MxBeanSample;
import com.ricequant.rqboot.jmx.shared_resource.JmxNames;
import org.apache.commons.io.FileUtils;

import javax.management.*;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chenfeng
 */
public class JmxBeanRegistry implements IJmxBeanRegistry {

  private final MBeanServer iBeanServer;

  private final JMXConnectorServer iConnectorServer;

  private final CommandRegistry iCommandRegistry;

  public JmxBeanRegistry(InetSocketAddress bind, String processName) throws Exception {
    iBeanServer = ManagementFactory.getPlatformMBeanServer();

    Map<String, Object> env = new HashMap<>();
    env.put("com.sun.management.jmxremote.authenticate", "false");
    env.put("com.sun.management.jmxremote.ssl", "false");
    env.put("com.sun.management.jmxremote", "true");
    JMXServiceURL url = new JMXServiceURL(
            "service:jmx:rmi:///jndi/rmi://" + bind.getHostString() + ":" + String.valueOf(bind.getPort()) +
                    "/jmxrmi");
    LocateRegistry.createRegistry(bind.getPort());
    iConnectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, env, iBeanServer);
    iConnectorServer.start();

    writeJmxUrl(url.toString(), processName);
    System.out.println("Jmx server started at: " + bind);

    iCommandRegistry = new CommandRegistry();
    DynamicMBean mBean = MBeanFactory.createBean(iCommandRegistry, iCommandRegistry.byBeanName(JmxNames.REGISTRY_NAME));

    try {
      iBeanServer.registerMBean(mBean, new ObjectName(JmxNames.REGISTRY_NAME));
    }
    catch (InstanceAlreadyExistsException e) {
      // ignore
    }
  }

  public static void main(String[] args) throws Exception {
    ServerSocket socket = new ServerSocket(58111);
    socket.close();

    JmxBeanRegistry registry = new JmxBeanRegistry(new InetSocketAddress(socket.getLocalPort()), "jmx server");
    registry.register(new BeanSample());
    MxBeanSample mxBean = new MxBeanSample();
    registry.register(mxBean);
    registry.registerDirectBean(mxBean);
    Thread.sleep(Long.MAX_VALUE);
  }

  private void writeJmxUrl(String urlString, String processName) {
    try {
      File tmpFile = File.createTempFile("test", null);
      String systemTmpFilePath = tmpFile.getParentFile().getAbsolutePath();
      if (!tmpFile.delete())
        System.err.println("Unable to delete temp file: " + tmpFile);

      File jmxFile = new File(systemTmpFilePath + File.separator + processName + ".jmx");
      FileUtils.write(jmxFile, urlString);

      System.out.println("Wrote jmx url to file: " + jmxFile.getAbsolutePath());
      jmxFile.deleteOnExit();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void registerDirectBean(Object mBean) throws JmxServerException {
    String name = mBean.getClass().getPackage().getName() + ":type=Direct" + mBean.getClass().getSimpleName();
    try {
      iBeanServer.registerMBean(mBean, new ObjectName(name));
    }
    catch (InstanceAlreadyExistsException e) {
      System.err.println("Bean already exists with name: " + name);
    }
    catch (Exception e) {
      throw new JmxServerException(e);
    }
  }

  @Override
  public String register(Object object) throws JmxServerException {

    if (object == null) {
      throw new IllegalArgumentException("No object specified.");
    }

    String name = object.getClass().getPackage().getName() + ":type=" + object.getClass().getSimpleName();
    IBeanCommandRegistry registry = iCommandRegistry.byBeanName(name);
    DynamicMBean mBean = MBeanFactory.createBean(object, registry);
    try {
      iBeanServer.registerMBean(mBean, new ObjectName(name));
    }
    catch (InstanceAlreadyExistsException e) {
      System.err.println("Bean already exists with name: " + name);
    }
    catch (Exception e) {
      throw new JmxServerException(e);
    }

    return name;
  }

  @Override
  public void deregister(String name) throws JmxServerException {

    if (name == null || "".equals(name)) {
      throw new IllegalArgumentException("No name specified.");
    }

    try {
      iBeanServer.unregisterMBean(new ObjectName(name));
    }
    catch (InstanceNotFoundException e) {

    }
    catch (Exception e) {
      throw new JmxServerException(e);
    }
  }

  @Override
  public boolean isRegistered(String name) throws JmxServerException {

    if (name == null || "".equals(name)) {
      throw new IllegalArgumentException("No name specified.");
    }

    try {
      return iBeanServer.isRegistered(new ObjectName(name));
    }
    catch (Exception e) {
      throw new JmxServerException(e);
    }

  }

  public void withdrawService() {
    try {
      iConnectorServer.stop();
    }
    catch (IOException e) {
      // ignore since stopping
    }
  }
}
