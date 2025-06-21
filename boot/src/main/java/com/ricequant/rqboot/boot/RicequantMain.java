package com.ricequant.rqboot.boot;

import com.ricequant.rqboot.config.cmd.CommandLineArgs;
import com.ricequant.rqboot.config.cmd.CommandLineParser;
import com.ricequant.rqboot.config.cmd.OptionMap;
import com.ricequant.rqboot.config.cmd.RicemapDefaultArgs;
import com.ricequant.rqboot.jmx.server.EmptyJmxBeanRegistry;
import com.ricequant.rqboot.jmx.server.IJmxBeanRegistry;
import com.ricequant.rqboot.jmx.server.JmxBeanRegistry;
import com.ricequant.rqboot.jmx.server.JmxServerException;
import com.ricequant.rqboot.lang.ClassLoadHelper;
import com.ricequant.rqboot.logging.LogConfiguration;
import com.ricequant.rqboot.logging.LogLevelJmx;

import java.io.File;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URL;
import java.security.Security;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.jar.Manifest;

/**
 * @author chenfeng
 */
public class RicequantMain implements IDaemonCallback {

  static {
    Security.setProperty("jdk.tls.disabledAlgorithms", "");
  }

  private static final String cApplicationClassKey = "ApplicationClass";

  protected IApplication iApplication;

  private Semaphore iShutDownSemaphore = new Semaphore(0);

  private IJmxBeanRegistry iBeanRegistry;

  public static void main(final String[] args) throws Exception {
    Security.setProperty("jdk.tls.disabledAlgorithms", "");
    boot(new RicequantMain(), args);
  }

  public static void boot(RicequantMain main, final String[] args) {
    if (reportVersion(args))
      return;

    run(main, args);
    main.setupHook();
  }

  private static boolean reportVersion(String[] args) {
    for (String arg : args) {
      if ("-v".equals(arg) || "--version".equals(arg)) {

        System.out.println(RicequantMain.class.getPackage().getSpecificationVersion());

        return true;
      }
    }
    return false;
  }

  private static void run(RicequantMain main, final String[] args) {
    main.init(args);
    try {
      main.start();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private IApplication findApplicationClassFromManifest() {
    try {
      Enumeration<URL> resources = RicequantMain.class.getClassLoader().getResources("META-INF/MANIFEST.MF");

      while (resources.hasMoreElements()) {
        InputStream content = resources.nextElement().openStream();
        Manifest manifest;
        try {
          manifest = new Manifest(content);
        }
        catch (Exception e) {
          continue;
        }

        String applicationClassString = manifest.getMainAttributes().getValue(cApplicationClassKey);
        if (applicationClassString != null) {
          applicationClassString = applicationClassString.replace(System.lineSeparator(), "");
          Class<?> applicationClass = Class.forName(applicationClassString);
          return (IApplication) applicationClass.newInstance();
        }

      }
    }
    catch (Exception e) {
      throw new RuntimeException("Error trying to find Application class from manifest", e);
    }

    return null;
  }

  protected List<Class<IApplication>> findAppImplementation() {
    return ClassLoadHelper.findImplInClassPath(IApplication.class, name -> true);
  }

  @Override
  public void init(String[] args) {
    System.out.println("Looking for implementation of IApplication...");

    iApplication = getApplication();

    if (iApplication == null)
      iApplication = findApplicationClassFromManifest();

    if (iApplication == null) {
      List<Class<IApplication>> apps = findAppImplementation();

      if (apps.size() == 0) {
        throw new RuntimeException("Cannot find any application implementations to load");
      }

      if (apps.size() != 1) {
        throw new RuntimeException(
                "One instance can load only one application implementation, actual " + apps.size() + ". "
                        + "Implementations are: " + apps);
      }

      try {
        iApplication = apps.getFirst().getDeclaredConstructor().newInstance();
      }
      catch (Throwable e) {
        throw new RuntimeException("Error instantiating application, must have default constructor available", e);
      }
      System.out.println("Found: " + apps.getFirst().getName());
    }

    CommandLineParser cp = new CommandLineParser(iApplication.getAppName());
    CommandLineArgs definedArgs = new CommandLineArgs();
    iApplication.customizeArgs(definedArgs);

    OptionMap optionMap = cp.parse(args, definedArgs);

    if (optionMap == null) {
      throw new ApplicationException("Fail to parse arguments: " + Arrays.toString(args));
    }

    changeWorkingDir(optionMap);

    LogConfiguration logConfig = new LogConfiguration(optionMap.getValue(RicemapDefaultArgs.DebugLevel),
            optionMap.getValue(RicemapDefaultArgs.RedirectConsole));

    try {
      iApplication.init(optionMap, args);
    }
    catch (Throwable e) {
      new ApplicationException("Application failed to be initialized.", e).printStackTrace();
      System.exit(-1);
    }

    try {
      if (!optionMap.isOptionSet(RicemapDefaultArgs.NoJmx))
        iBeanRegistry = startJmxServer(logConfig, optionMap);
      else
        System.out.println("Jmx server is disabled");
    }
    catch (Throwable e) {
      System.err.println("Application failed to start jmx server.");
      e.printStackTrace();
    }

    if (iBeanRegistry == null)
      iBeanRegistry = new EmptyJmxBeanRegistry();
  }

  private void changeWorkingDir(OptionMap optionMap) {
    String cwd = optionMap.isOptionSet(RicemapDefaultArgs.Cwd) ? optionMap.getValue(RicemapDefaultArgs.Cwd)
            : System.getProperty("user.dir");

    File absoluteCwd = new File(cwd).getAbsoluteFile();

    if (absoluteCwd.exists() || absoluteCwd.mkdir()) {
      System.setProperty("user.dir", absoluteCwd.getAbsolutePath());
      System.out.println("Set working directory to: " + absoluteCwd.getAbsolutePath());
    }
    else
      System.err.println("Unable to determine the current working directory, use default");
  }

  protected IApplication getApplication() {
    return null;
  }

  private void setupHook() {
    ShutDownHook hookThread = new ShutDownHook();
    Runtime.getRuntime().addShutdownHook(hookThread);

    iShutDownSemaphore.acquireUninterruptibly();
  }

  @Override
  public void start() throws Exception {
    try {
      iBeanRegistry.register(new VMStatsReporter());
      iApplication.start(iBeanRegistry);
    }
    catch (JmxServerException jmxe) {
      System.err.println("Unable to register jmx bean");
      jmxe.printStackTrace();
    }
    catch (Throwable e) {
      new ApplicationException("Application failed to start.", e).printStackTrace();
      System.exit(-2);
    }
  }

  @Override
  public void stop() {
    iApplication.tearDown();
    iBeanRegistry.withdrawService();
    iShutDownSemaphore.release();
  }


  @Override
  public void destroy() {

  }

  /**
   * Override this function if your application requires another implementation of starting JmxBeanRegistry. Return
   * null, if your application does not need it.
   *
   * @param logConfig
   * @param optionMap parsed command line arguments in a map
   * @return the JmxBeanRegistry
   * @throws Exception
   */
  protected JmxBeanRegistry startJmxServer(LogConfiguration logConfig, OptionMap optionMap) throws Exception {
    String host = optionMap.getValue(RicemapDefaultArgs.JmxHost);
    int port = Integer.parseInt(optionMap.getValue(RicemapDefaultArgs.JmxPort));

    if (port == 0) {
      ServerSocket server = new ServerSocket(port);
      port = server.getLocalPort();
      server.close();
    }

    String processName = optionMap.getValue(RicemapDefaultArgs.InstanceName);
    if (processName == null)
      processName = getApplication().getAppName();

    JmxBeanRegistry manager = new JmxBeanRegistry(new InetSocketAddress(host, port), processName);

    manager.register(new LogLevelJmx(logConfig));
    return manager;
  }

  private class ShutDownHook extends Thread {

    @Override
    public void run() {
      try {
        RicequantMain.this.stop();
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
