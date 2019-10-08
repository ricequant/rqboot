package com.ricequant.rqboot.lang.factory;

import java.util.function.BiFunction;

/**
 * @author chenfeng
 */
public final class OneParameterGetOrCreateFactory<KeyType, ObjectType, ParameterType>
        extends AbstractGetOrCreateFactory<KeyType, ObjectType> {

  private final BiFunction<KeyType, ParameterType, ObjectType> iCreator;

  public OneParameterGetOrCreateFactory(BiFunction<KeyType, ParameterType, ObjectType> creator) {
    iCreator = creator;
  }

  public final ObjectType getOrCreate(KeyType key, ParameterType param) {
    ObjectType value = iMap.get(key);
    if (value == null) {
      synchronized (iMap) {
        value = iMap.get(key);
        if (value == null) {
          value = iCreator.apply(key, param);
          iMap.put(key, value);
        }
      }
    }

    return value;
  }

}
