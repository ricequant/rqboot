package com.ricequant.rqboot.lang.factory;

import java.util.Map;
import java.util.function.Function;

/**
 * @author chenfeng
 */
public class ExternalMapGetOrCreateFactory<KeyType, ValueType> extends GetOrCreateFactory<KeyType, ValueType> {

  public ExternalMapGetOrCreateFactory(Function<KeyType, ValueType> creator, Map<KeyType, ValueType> map) {
    super(creator, map);
  }
}
