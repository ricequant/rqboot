package com.ricequant.rqboot.lang.factory;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Function;

/**
 * @author Jeffery
 */
public final class LimitSizeGetOrCreateFactory<KeyType, ObjectType>
        extends AbstractGetOrCreateFactory<KeyType, ObjectType> {

  private final Function<KeyType, ObjectType> iCreator;

  private final int iCapacity;

  private final Deque<KeyType> iKeyArray;

  public LimitSizeGetOrCreateFactory(Function<KeyType, ObjectType> creator, int capacity) {
    iCreator = creator;
    iCapacity = capacity;
    iKeyArray = new ArrayDeque<>();
  }

  public final ObjectType getOrCreate(KeyType key) {
    ObjectType value = iMap.get(key);
    if (value == null) {
      synchronized (iMap) {
        value = iMap.get(key);
        if (value == null) {
          if (iKeyArray.size() >= iCapacity) {
            remove(iKeyArray.getFirst());
            iKeyArray.removeFirst();
          }

          value = iCreator.apply(key);
          iMap.put(key, value);
          iKeyArray.addLast(key);
        }
      }
    }

    return value;
  }

  public final void clearAll() {
    synchronized (iMap) {
      iMap.clear();
      iKeyArray.clear();
    }
  }
}
