package com.ricequant.rqboot.lang.factory;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author chenfeng
 */
public abstract class AbstractGetOrCreateFactory<KeyType, ObjectType> {

  protected Map<KeyType, ObjectType> iMap;

  public AbstractGetOrCreateFactory() {
    iMap = new ConcurrentHashMap<>();
  }

  public AbstractGetOrCreateFactory(Map<KeyType, ObjectType> map) {
    iMap = map;
  }

  public Set<KeyType> keySet() {
    return iMap.keySet();
  }

  public ObjectType get(KeyType key) {
    return iMap.get(key);
  }

  public final ObjectType put(KeyType key, ObjectType object) {
    synchronized (iMap) {
      return iMap.put(key, object);
    }
  }

  public final ObjectType remove(KeyType key) {
    synchronized (iMap) {
      return iMap.remove(key);
    }
  }

  public final boolean contains(KeyType key) {
    synchronized (iMap) {
      return iMap.containsKey(key);
    }
  }

  public void forAllValues(Consumer<ObjectType> consumer) {
    synchronized (iMap) {
      iMap.values().forEach(consumer::accept);
    }
  }

  public void forAllKeys(Consumer<KeyType> consumer) {
    synchronized (iMap) {
      iMap.keySet().forEach(consumer);
    }
  }

  public int size() {
    return iMap.size();
  }

  public void forAllEntries(BiConsumer<KeyType, ObjectType> consumer) {
    synchronized (iMap) {
      iMap.entrySet().forEach((entry -> {
        consumer.accept(entry.getKey(), entry.getValue());
      }));
    }
  }

  public Set<Map.Entry<KeyType, ObjectType>> entries() {
    return iMap.entrySet();
  }

  public final void clear() {
    synchronized (iMap) {
      iMap.clear();
    }
  }

  public Collection<ObjectType> values() {
    return iMap.values();
  }
}
