package com.ricequant.rqboot.jmx.server;


import com.ricequant.rqboot.jmx.shared_resource.annotations.JmxBean;
import com.ricequant.rqboot.jmx.shared_resource.annotations.JmxMethod;
import com.ricequant.rqboot.jmx.shared_resource.registry.*;
import com.ricequant.rqboot.lang.factory.GetOrCreateFactory;

import javax.management.MalformedObjectNameException;
import java.util.List;

/**
 * @author chenfeng
 */

@JmxBean("Registry of all commands")
public class CommandRegistry {

  private final GetOrCreateFactory<String, BeanCommandRegistry> iBeanRegistry =
          new GetOrCreateFactory<>(BeanCommandRegistry::new);

  private final JmxCommandList iCommandList = new JmxCommandList();

  public static JmxType parseType(Class<?> returnType) {
    if (returnType.isArray())
      return JmxType.arrayType(JmxTypeEnum.fromType(returnType));
    else
      return JmxType.simpleType(JmxTypeEnum.fromType(returnType));
  }

  public IBeanCommandRegistry byBeanName(String name) {
    return iBeanRegistry.getOrCreate(name);
  }

  @JmxMethod("List all commands")
  public JmxCommandList getList() throws MalformedObjectNameException {
    return iCommandList;
  }

  private class BeanCommandRegistry implements IBeanCommandRegistry {

    private final String iBeanName;

    BeanCommandRegistry(String beanName) {
      iBeanName = beanName;
    }

    @Override
    public void addOperation(String name, String description, List<JmxArg> args, Class<?> returnType) {
      JmxCommand command = JmxCommand.operation(iBeanName, name, description, parseType(returnType), args);
      iCommandList.addCommand(command);
    }

    @Override
    public void addAttribute(String name, Class<?> attributeType, String description, boolean isReadable,
            boolean isWritable) {
      JmxCommand command =
              JmxCommand.attribute(iBeanName, name, description, parseType(attributeType), isReadable, isWritable);
      iCommandList.addCommand(command);
    }
  }


}
