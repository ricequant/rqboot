package com.ricequant.rqboot.jmx.client;


import com.ricequant.rqboot.jmx.shared_resource.JmxNames;
import com.ricequant.rqboot.jmx.shared_resource.registry.*;

import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.util.*;

/**
 * @author chenfeng
 */
public class JmxCommandExecutor {

  private final MBeanServerConnection iServerConnection;

  public JmxCommandExecutor(MBeanServerConnection serverConnection) {
    iServerConnection = serverConnection;
  }

  private static String printVariable(Object var, JmxType type) {
    StringBuilder sb = new StringBuilder();
    if (type.isArray()) {
      Object[] varArray = (Object[]) var;
      sb.append("[");
      for (int i = 0; i < varArray.length; i++) {
        Object o = varArray[i];
        printBaseType(o, type, sb);
        if (i != varArray.length - 1)
          sb.append(",");
      }
      sb.append("]");
    }
    else
      printBaseType(var, type, sb);

    return sb.toString();
  }

  private static void printBaseType(Object var, JmxType type, StringBuilder sb) {
    switch (type.getBaseType()) {
      case STRING:
        sb.append("\"").append(var).append("\"");
        break;
      case VOID:
        break;
      default:
        sb.append(var);
    }
  }

  private static JmxCommand resolveConflicts(List<JmxCommand> commands) {
    for (int i = 0; i < commands.size(); i++) {
      JmxCommand command = commands.get(i);
      System.out.println((i + 1) + ".\t" + command);
    }
    System.out.println("I am confused... Please select the index of command you want to execute: ");
    Scanner in = new Scanner(System.in);
    int index = in.nextInt();
    if (index < 1 || index > commands.size()) {
      System.out.println("Index out of range");
      return null;
    }
    return commands.get(index - 1);
  }

  private static Map<JmxCommandFeature, List<JmxCommand>> resolveCommands(JmxCommandList commandsList) {
    Map<JmxCommandFeature, List<JmxCommand>> commandMap = new HashMap<>();

    for (JmxCommand command : commandsList.getCommands()) {
      JmxCommandFeature feature = command.getFeature();
      List<JmxCommand> list = commandMap.get(feature);

      if (list == null) {
        list = new ArrayList<>();
        commandMap.put(feature, list);
      }

      list.add(command);
    }

    return commandMap;
  }

  public Object execute(String command, String... args) {
    try {
      Object commandsObject = iServerConnection.getAttribute(new ObjectName(JmxNames.REGISTRY_NAME), "list");
      JmxCommandList commandsList = (JmxCommandList) commandsObject;

      Map<JmxCommandFeature, List<JmxCommand>> commandsMap = resolveCommands(commandsList);

      List<JmxCommand> commands = commandsMap.get(new JmxCommandFeature(command, args.length));
      if (args.length == 1 && commands == null)
        commands = commandsMap.get(new JmxCommandFeature(command, 0));

      if (commands == null) {
        System.err.println("Command not found: " + command);
      }
      else if (commands.size() != 1) {
        JmxCommand commandToExecute = resolveConflicts(commands);
        return executeCommand(commandToExecute, args);
      }
      else {
        JmxCommand commandToExecute = commands.get(0);
        return executeCommand(commandToExecute, args);
      }
    }
    catch (Exception e) {
      throw new RuntimeException("Unexcepted jmx error", e);
    }
    return null;
  }

  private Object executeCommand(JmxCommand command, String... args) throws Exception {
    if (command == null)
      throw new RuntimeException("Unresolvable command");

    if (command.isAttribute()) {
      if (args.length == 0) {
        if (command.isReadable()) {
          Object result = iServerConnection.getAttribute(new ObjectName(command.getObjectName()), command.getName());
          System.out.println(printVariable(result, command.getReturnValue().getType()));
          return result;
        }
        else
          System.out.println("Sorry, attribute is not readable");
      }
      else if (args.length == 1) {
        if (command.isWritable()) {
          iServerConnection.setAttribute(new ObjectName(command.getObjectName()), new Attribute(command.getName(),
                  command.getReturnValue().getType().getBaseType().fromString(args[0])));
        }
      }
    }
    else {
      List<JmxArg> definedArgs = command.getArgs();
      List<String> signatures = new ArrayList<>();
      for (JmxArg definedArg : definedArgs) {
        signatures.add(definedArg.getType().getBaseType().getSignature());
      }

      Object[] argsConverted = new Object[args.length];
      for (int i = 0; i < args.length; i++) {
        argsConverted[i] = definedArgs.get(i).getType().getBaseType().fromString(args[i]);
      }

      Object result = iServerConnection
              .invoke(new ObjectName(command.getObjectName()), command.getName(), argsConverted,
                      signatures.toArray(new String[signatures.size()]));
      System.out.println(printVariable(result, command.getReturnValue().getType()));
      return result;
    }
    return null;
  }
}
