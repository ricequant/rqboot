package com.ricequant.rqboot.jmx.client;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;

/**
 * @author chenfeng
 */
public class JmxClientMain {

  private JMXConnector iConnector;

  private MBeanServerConnection iServerConnection;

  public static void main(String[] args) throws Exception {
    // start(args[0], Integer.parseInt(args[1]));

    JmxClientMain client = new JmxClientMain();
    client.start("localhost", 58001);
    //client.execute("listClients");
    client.execute("checkClientStatus", "13404");
  }

  public void start(JMXServiceURL url) throws IOException {
    iConnector = JMXConnectorFactory.connect(url, null);
    iServerConnection = iConnector.getMBeanServerConnection();
  }

  public void start(String host, int port) throws IOException {
    JMXServiceURL url =
            new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi");

    start(url);
  }

  public Object execute(String command, String... args) throws Exception {
    return new JmxCommandExecutor(iServerConnection).execute(command, args);
  }
}
