package com.ricequant.rqboot.jmx.shared_resource.registry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chenfeng
 */
public class JmxCommandList implements Serializable {

  private final List<JmxCommand> iCommandList = new ArrayList<>();

  public void addCommand(JmxCommand command) {
    iCommandList.add(command);
  }

  public List<JmxCommand> getCommands() {
    return iCommandList;
  }

  @Override
  public String toString() {
    Map<String, List<JmxCommand>> commandMap = new HashMap<>();
    for (JmxCommand command : iCommandList) {
      List<JmxCommand> list = commandMap.get(command.getObjectName());
      if (list == null) {
        list = new ArrayList<>();
        commandMap.put(command.getObjectName(), list);
      }
      list.add(command);
    }

    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, List<JmxCommand>> entry : commandMap.entrySet()) {
      String objectName = entry.getKey();
      String printableName = objectName.substring(objectName.indexOf("=") + 1);
      sb.append(printableName).append(":").append(System.lineSeparator());

      for (JmxCommand command : entry.getValue()) {
        sb.append("\tcommand: ").append(command.getName()).append(System.lineSeparator());
        sb.append("\t\targs: ").append(command.getArgs()).append(System.lineSeparator());
        if (command.getReturnValue().getType().getBaseType() != JmxTypeEnum.VOID)
          sb.append("\t\treturns: ").append(command.getReturnValue().getType()).append(System.lineSeparator());
      }
    }

    return sb.toString();
  }
}
