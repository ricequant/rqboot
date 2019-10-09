package com.ricequant.rqboot.jmx.jmx_cmd;

import com.ricequant.rqboot.config.cmd.RicemapDefaultArgs;
import com.ricequant.rqboot.config.processes.RQConfig;
import com.ricequant.rqboot.config.processes.RQProcessConfig;
import com.ricequant.rqboot.jmx.client.JmxClientMain;
import org.apache.commons.io.FileUtils;

import javax.management.remote.JMXServiceURL;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author chenfeng
 */
public class JmxCMDMain {

  public static void main(String[] args) throws Exception {
    if (args.length < 2 || args[0].equals("-h") || args[0].equals("--help") || args[0].equals("-help")) {
      System.out.println("Usage: jcmd [options...] [processName] <command> [args...]");
      System.out.println("Options are:" + System.lineSeparator() + "\t -c,\tpath to rq.config"

              + System.lineSeparator() + "\t -ep \tspecify jmx url instead of searching from rq.config");
      return;
    }

    String firstArg = args[0];

    JmxClientMain client = new JmxClientMain();

    int commandOffset = 1;

    if (firstArg.equals("-ep")) {
      String ep = args[1];
      String host = ep.split(":")[0];
      int port = Integer.parseInt(ep.split(":")[1]);
      commandOffset = 2;
      client.start(host, port);
    }
    else {
      String rqConfigPath = "/etc/rq/rq.config";
      if (firstArg.equals("-c")) {
        rqConfigPath = args[1];
        commandOffset = 3;
      }

      RQConfig config = RQConfig.fromConfigFile(new File(rqConfigPath));

      String processName = args[commandOffset - 1];

      RQProcessConfig process = null;
      for (RQProcessConfig p : config.getProcesses()) {
        if (p.getName().equals(processName)) {
          process = p;
          break;
        }
      }

      if (process == null) {
        System.err.println("Unable to find process: " + processName);
        return;
      }

      String jmxURL = getJmxURL(process, config.getGlobal().getTemp());
      if (jmxURL == null) {
        String jmxHost = parseArgValue(process.getArgs(), RicemapDefaultArgs.JmxHost.getShortName());
        String jmxPort = parseArgValue(process.getArgs(), RicemapDefaultArgs.JmxPort.getShortName());

        if (jmxHost == null)
          jmxHost = "127.0.0.1";

        if (jmxPort == null)
          throw new RuntimeException("Unable to find jmx server end points");

        client.start(jmxHost, Integer.parseInt(jmxPort));
      }
      else
        client.start(new JMXServiceURL(jmxURL));
    }
    String command = args[commandOffset];
    String[] commandArgs = Arrays.copyOfRange(args, commandOffset + 1, args.length);

    client.execute(command, commandArgs);
  }


  private static String getJmxURL(RQProcessConfig config, String tmpDir) {
    File urlFile = new File(tmpDir + File.separator + config.getName() + ".jmx");
    try {
      return FileUtils.readFileToString(urlFile);
    }
    catch (IOException e) {
      return null;
    }
  }

  private static String parseArgValue(String argsString, String argName) {
    int argIndex = argsString.indexOf(argName);
    if (argIndex >= 0) {
      int endSpace = argsString.indexOf(" ", argIndex + argName.length() + 1);
      if (endSpace > 0)
        return argsString.substring(argIndex + argName.length() + 1, endSpace);
      else
        return argsString.substring(argIndex + argName.length() + 1);
    }

    return null;
  }
}
