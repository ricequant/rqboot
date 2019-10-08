package com.ricequant.rqboot.lang.factory;

import java.util.Map;
import java.util.function.Function;

/**
 * @author chenfeng
 */
public class GetOrCreateFactory<KeyType, ObjectType> extends AbstractGetOrCreateFactory<KeyType, ObjectType> {

  private final Function<KeyType, ObjectType> iCreator;

  public GetOrCreateFactory(Function<KeyType, ObjectType> creator) {
    iCreator = creator;
  }

  public GetOrCreateFactory(Function<KeyType, ObjectType> creator, Map<KeyType, ObjectType> map) {
    super(map);
    iCreator = creator;
  }

  public final ObjectType getOrCreate(KeyType key) {
    ObjectType value = iMap.get(key);
    if (value == null) {
      synchronized (iMap) {
        value = iMap.get(key);
        if (value == null) {
          value = iCreator.apply(key);
          iMap.put(key, value);
        }
      }
    }

    return value;
  }

}
