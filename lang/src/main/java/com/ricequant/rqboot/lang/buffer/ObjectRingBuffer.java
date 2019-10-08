package com.ricequant.rqboot.lang.buffer;

import java.lang.reflect.Array;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author chenfeng
 */
public class ObjectRingBuffer<T> {

  private final int iSize;

  private final T[] iBuffer;

  private int iLatest;

  private Supplier<T> iNullObjectFactory;

  @SuppressWarnings("unchecked")
  public ObjectRingBuffer(int size) {
    iSize = size;
    iBuffer = (T[]) new Object[iSize];
  }

  public ObjectRingBuffer(int size, Supplier<T> nullObjectFactory) {
    this(size);
    iNullObjectFactory = nullObjectFactory;
    for (int i = 0; i < iBuffer.length; i++)
      iBuffer[i] = nullObjectFactory.get();
  }

  @SuppressWarnings("unchecked")
  public void clear() {
    for (int i = 0; i < iBuffer.length; i++)
      iBuffer[i] = iNullObjectFactory.get();
  }

  public T getLatest() {
    return iBuffer[iLatest];
  }

  public T getOldest() {
    int index = iLatest + 1;
    if (index >= iSize)
      index -= iSize;
    return iBuffer[index];
  }

  public T append(T ele) {
    iLatest += 1;
    if (iLatest >= iSize)
      iLatest = iLatest - iSize;

    T oldValue = iBuffer[iLatest];
    iBuffer[iLatest] = ele;

    return oldValue;
  }

  public final int getSize() {
    return iSize;
  }

  public T getLatestNthItem(int n) {
    checkLatestNInput(n, n);

    int index = iLatest - n + 1;
    if (index < 0)
      index += iSize;

    return iBuffer[index];
  }

  public void forLatestNItems(int n, Consumer<T> consumer) {
    checkLatestNInput(n, n);

    int startIndex = iLatest - n + 1;
    if (startIndex < 0)
      startIndex += iSize;

    for (int i = 0; i < n; i++) {
      int index = startIndex + i;
      if (index >= iSize)
        index -= iSize;

      consumer.accept(iBuffer[index]);
    }
  }

  public void forLatestItems(int n, int length, Consumer<T> consumer) {
    checkLatestNInput(n, length);

    int startIndex = iLatest - n + 1;
    if (startIndex < 0)
      startIndex += iSize;

    int count = 0;
    for (int i = 0; i < n; i++) {
      int index = startIndex + i;
      if (index >= iSize)
        index -= iSize;

      consumer.accept(iBuffer[index]);
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

    int startIndex = iLatest - n + 1;

    if (startIndex < 0)
      startIndex += iSize;

    int bufferIndex = startIndex;

    T[] ret = a;
    if (a.length < length)
      //noinspection unchecked
      ret = (T[]) Array.newInstance(a.getClass().getComponentType(), length);

    // TODO: performance can be optimized by using System.arraycopy
    for (int i = 0; i < length; i++) {
      ret[i] = iBuffer[bufferIndex];

      bufferIndex++;

      if (bufferIndex == iSize)
        bufferIndex = 0;
    }

    return ret;
  }

  private void checkLatestNInput(int n, int length) {
    if (n <= 0)
      throw new IllegalArgumentException(
              "n must be greater than 0. n=1 means \"the latest\", n=2 means \"the second latest\"");

    if (n > iSize)
      throw new IndexOutOfBoundsException("The buffer only has " + iSize + " slots, but wanted " + n + " items");

    if (length <= 0)
      throw new IllegalArgumentException("length must be greater than 0");

    if (n < length)
      throw new IndexOutOfBoundsException("length must be greater than or equal to n");
  }
}
