package com.ricequant.rqboot.jmx.server;


import com.ricequant.rqboot.jmx.shared_resource.registry.JmxArg;

import java.util.List;

/**
 * @author chenfeng
 */
public interface IBeanCommandRegistry {

  void addOperation(String name, String description, List<JmxArg> args, Class<?> returnType);

  void addAttribute(String name, Class<?> attributeType, String description, boolean isReadable, boolean isWritable);
}
