package com.ricequant.rqboot.lang.factory;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author chenfeng
 */
public abstract class AbstractGetOrCreateFactory<KeyType, ObjectType> {

  protected Map<KeyType, ObjectType> iMap;

  private final ReentrantLock lock = new ReentrantLock();

  public AbstractGetOrCreateFactory() {
    iMap = new ConcurrentHashMap<>();
  }

  public AbstractGetOrCreateFactory(Map<KeyType, ObjectType> map) {
    iMap = map;
  }

  public Set<KeyType> keySet() {
    try {
      lock.lock();
      return iMap.keySet();
    }
    finally {
      lock.unlock();
    }
  }

  public ObjectType get(KeyType key) {
    try {
      lock.lock();
      return iMap.get(key);
    }
    finally {
      lock.unlock();
    }
  }

  public void lock() {
    lock.lock();
  }

  public void unlock() {
    lock.unlock();
  }

  public final ObjectType put(KeyType key, ObjectType object) {
    try {
      lock.lock();
      return iMap.put(key, object);
    }
    finally {
      lock.unlock();
    }
  }

  public final ObjectType remove(KeyType key) {
    try {
      lock.lock();
      return iMap.remove(key);
    }
    finally {
      lock.unlock();
    }
  }

  public final boolean contains(KeyType key) {
    try {
      lock.lock();
      return iMap.containsKey(key);
    }
    finally {
      lock.unlock();
    }
  }

  public void forAllValues(Consumer<ObjectType> consumer) {
    try {
      lock.lock();
      iMap.values().forEach(consumer::accept);
    }
    finally {
      lock.unlock();
    }
  }

  public void forAllKeys(Consumer<KeyType> consumer) {
    try {
      lock.lock();
      iMap.keySet().forEach(consumer);
    }
    finally {
      lock.unlock();
    }
  }

  public int size() {
    try {
      lock.lock();
      return iMap.size();
    }
    finally {
      lock.unlock();
    }
  }

  public void forAllEntries(BiConsumer<KeyType, ObjectType> consumer) {
    try {
      lock.lock();
      iMap.entrySet().forEach((entry -> {
        consumer.accept(entry.getKey(), entry.getValue());
      }));
    }
    finally {
      lock.unlock();
    }
  }

  public Set<Map.Entry<KeyType, ObjectType>> entries() {
    try {
      lock.lock();
      return iMap.entrySet();
    }
    finally {
      lock.unlock();
    }
  }

  public final void clear() {
    try {
      lock.lock();
      iMap.clear();
    }
    finally {
      lock.unlock();
    }
  }

  public Collection<ObjectType> values() {
    try {
      lock.lock();
      return iMap.values();
    }
    finally {
      lock.unlock();
    }
  }
}
