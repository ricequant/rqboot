package com.ricequant.rqboot.lang.buffer;

import java.lang.reflect.Array;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author chenfeng
 */
public class ObjectRingBuffer<T> {

  private final int size;

  private final T[] buffer;

  private int last;

  private int count;

  private Supplier<T> nullObjectFactory;

  @SuppressWarnings("unchecked")
  public ObjectRingBuffer(int size) {
    if (size <= 0) {
      throw new IllegalArgumentException("Size must be positive, got: " + size);
    }
    this.size = size;
    buffer = (T[]) new Object[this.size];
    this.last = -1;
    this.count = 0;
  }

  public ObjectRingBuffer(int size, Supplier<T> nullObjectFactory) {
    this(size);
    this.nullObjectFactory = nullObjectFactory;
    for (int i = 0; i < buffer.length; i++)
      buffer[i] = nullObjectFactory.get();
  }

  @SuppressWarnings("unchecked")
  public void clear() {
    if (nullObjectFactory != null) {
      for (int i = 0; i < buffer.length; i++)
        buffer[i] = nullObjectFactory.get();
    }
    this.last = -1;
    this.count = 0;
  }

  public T getLatest() {
    if (count == 0) {
      throw new IllegalStateException("Buffer is empty");
    }
    return buffer[last];
  }

  public T getOldest() {
    if (count == 0) {
      throw new IllegalStateException("Buffer is empty");
    }
    // If buffer is not full, oldest is at index 0
    // If buffer is full, oldest is at (last + 1) % size
    if (count < size) {
      return buffer[0];
    }
    int index = last + 1;
    if (index >= size)
      index -= size;
    return buffer[index];
  }

  public T append(T ele) {
    last += 1;
    if (last >= size)
      last = last - size;

    T oldValue = buffer[last];
    buffer[last] = ele;

    if (count < size)
      count++;

    return oldValue;
  }

  public final int size() {
    return size;
  }

  public T getLatestNthItem(int n) {
    checkLatestNInput(n, n);

    int index = last - n + 1;
    if (index < 0)
      index += size;

    return buffer[index];
  }

  public void forLatestNItems(int n, Consumer<T> consumer) {
    checkLatestNInput(n, n);

    int startIndex = last - n + 1;
    if (startIndex < 0)
      startIndex += size;

    for (int i = 0; i < n; i++) {
      int index = startIndex + i;
      if (index >= size)
        index -= size;

      consumer.accept(buffer[index]);
    }
  }

  public void forLatestItems(int n, int length, Consumer<T> consumer) {
    checkLatestNInput(n, length);

    int startIndex = last - n + 1;
    if (startIndex < 0)
      startIndex += size;

    int count = 0;
    for (int i = 0; i < n; i++) {
      int index = startIndex + i;
      if (index >= size)
        index -= size;

      consumer.accept(buffer[index]);
      if (++count == length)
        break;
    }
  }

  /**
   * Returns a new instance of array containing references to the elements in the buffer.
   *
   * @param n
   *         the starting index of the buffer, it counts from the latest position.
   * @param length
   *         the number of elements you want to copy. It counts from the point which indicated by "n" parameter
   *         incrementally.
   *
   * @return a new instance of the array containing element references in the buffer. If the ring buffer is not fully
   * filled, it may contain "null".
   */
  public T[] toArrayFromLatestN(int n, int length, T[] a) {

    checkLatestNInput(n, length);

    int startIndex = last - n + 1;

    if (startIndex < 0)
      startIndex += size;

    int bufferIndex = startIndex;

    T[] ret = a;
    if (a.length < length)
      //noinspection unchecked
      ret = (T[]) Array.newInstance(a.getClass().getComponentType(), length);

    // TODO: performance can be optimized by using System.arraycopy
    for (int i = 0; i < length; i++) {
      ret[i] = buffer[bufferIndex];

      bufferIndex++;

      if (bufferIndex == size)
        bufferIndex = 0;
    }

    return ret;
  }

  public int count() {
    return count;
  }

  public boolean isFull() {
    return count >= size;
  }

  private void checkLatestNInput(int n, int length) {
    if (n <= 0)
      throw new IllegalArgumentException(
              "n must be greater than 0. n=1 means \"the latest\", n=2 means \"the second latest\"");

    if (n > size)
      throw new IndexOutOfBoundsException("The buffer only has " + size + " slots, but wanted " + n + " items");

    if (length <= 0)
      throw new IllegalArgumentException("length must be greater than 0");

    if (n < length)
      throw new IndexOutOfBoundsException("length must be less than or equal to n");
  }
}
