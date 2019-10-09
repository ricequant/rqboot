package com.ricequant.rqboot.config.cmd;

import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author chenfeng
 */
public class OptionMap {

  private final Map<String, Set<String>> iOptMap;

  public OptionMap() {
    iOptMap = new ConcurrentHashMap<>();
  }

  void put(String opt, String[] values) {
    Set<String> set = iOptMap.get(opt);
    if (set == null) {
      set = new LinkedHashSet<>();
      iOptMap.put(opt, set);
    }

    for (String newValue : values) {
      set.add(newValue);
    }
  }

  void put(String opt, String value) {
    put(opt, new String[]{value});
  }

  public Set<String> getValues(IArgument opt) {
    Set<String> set = iOptMap.get(opt.getShortName());

    if (set == null && opt.getDefaultValue() == null)
      return Collections.emptySet();

    if (set == null) {
      set = new HashSet<>(Arrays.asList(opt.getDefaultValue().split(opt.getMultipleArgSeparator())));
    }
    return set;
  }

  public String getValue(IArgument opt) {
    Set<String> set = iOptMap.get(opt.getShortName());
    if (set == null)
      return opt.getDefaultValue();

    return StringUtils.join(set, opt.getMultipleArgSeparator() == null ? ";" : opt.getMultipleArgSeparator());
  }

  public String getValue(IArgument opt, String defaultValue) {
    Set<String> set = iOptMap.get(opt.getShortName());
    if (set == null)
      return defaultValue;

    return StringUtils.join(set, opt.getMultipleArgSeparator() == null ? ";" : opt.getMultipleArgSeparator());
  }

  public boolean isOptionSet(IArgument opt) {
    return iOptMap.containsKey(opt.getShortName());
  }

}
