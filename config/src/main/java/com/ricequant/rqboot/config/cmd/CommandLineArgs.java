package com.ricequant.rqboot.config.cmd;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.util.*;

/**
 * @author chenfeng
 */
public class CommandLineArgs {

  private final Options iOptions;

  private final Map<String, IArgument> iNameArgumentMap;

  private final List<IArgument> iArgumentList;

  public CommandLineArgs() {
    iOptions = new Options();
    iNameArgumentMap = new HashMap<>();
    iArgumentList = new ArrayList<>();

    withArguments(RicemapDefaultArgs.Help);
    withArguments(RicemapDefaultArgs.InstanceName);
  }

  public void withDefaultOptions() {
    withArguments(RicemapDefaultArgs.DevMode, RicemapDefaultArgs.ConfigFile, RicemapDefaultArgs.TempDir,
            RicemapDefaultArgs.DebugLevel, RicemapDefaultArgs.JmxPort, RicemapDefaultArgs.JmxHost,
            RicemapDefaultArgs.Cwd, RicemapDefaultArgs.IsStatOpen, RicemapDefaultArgs.SerializerType);
  }

  public void withArguments(IArgument... arguments) {
    if (arguments == null)
      return;

    for (IArgument argument : arguments) {
      checkExistence(argument);
      iOptions.addOption(argument.getShortName(), argument.getLongName(), !argument.isFlag(),
              argument.getDescription());
      iArgumentList.add(argument);
    }
  }

  public void withServiceRegistry() {
    withArguments(RicemapDefaultArgs.ServiceRegistry);
  }

  Options getOptions() {
    return iOptions;
  }

  private void checkExistence(IArgument argument) {
    if (iNameArgumentMap.containsKey(argument.getShortName()))
      throw new IllegalArgumentException("Argument short name " + argument.getShortName() + " already exists");

    if (iNameArgumentMap.containsKey(argument.getLongName()))
      throw new IllegalArgumentException("Argument long name " + argument.getLongName() + " already exists");

    iNameArgumentMap.put(argument.getShortName(), argument);
    iNameArgumentMap.put(argument.getLongName(), argument);
  }

  public List<String> validate(CommandLine line) {
    List<String> reasons = new LinkedList<>();
    for (IArgument arg : iArgumentList) {
      if (arg.isRequired() && !line.hasOption(arg.getShortName())) {
        reasons.add("Argument <" + arg.getShortName() + "> is required but not input");
        continue;
      }

      if (arg.isFlag() && line.hasOption(arg.getShortName()) && line.getOptionValues(arg.getShortName()) != null) {
        reasons.add("Argument <" + arg.getShortName() + "> is defined as flag but has value: " + line.getOptionValue(
                arg.getShortName()));
        continue;
      }

      if (line.hasOption(arg.getShortName()) && line.getOptionValues(arg.getShortName()) != null
              && arg.getMaxCount() > 0 && line.getOptionValues(arg.getShortName()).length > arg.getMaxCount()) {
        reasons.add("Argument <" + arg.getShortName() + "> can have maximum count of " + arg.getMaxCount() + ", "
                + "but there are " + line.getOptionValues(arg.getShortName()).length);
      }
    }

    return reasons;
  }
}
