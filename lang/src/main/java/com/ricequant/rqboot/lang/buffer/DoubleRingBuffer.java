package com.ricequant.rqboot.lang.buffer;

import java.util.function.DoubleConsumer;

/**
 * A fixed-size circular buffer optimized for primitive double values.
 * This class provides efficient storage and retrieval of double values without boxing overhead.
 * <p>
 * The buffer maintains a fixed capacity and overwrites the oldest values when full.
 * All values are initially 0.0.
 *
 * @author kangol
 */
public class DoubleRingBuffer {

  private final int size;

  private final double[] buffer;

  private int last;

  private int count;

  /**
   * Creates a new DoubleRingBuffer with the specified size.
   * All values are initialized to 0.0.
   *
   * @param size the capacity of the buffer, must be positive
   */
  public DoubleRingBuffer(int size) {
    if (size <= 0) {
      throw new IllegalArgumentException("Size must be positive, got: " + size);
    }
    this.size = size;
    this.buffer = new double[this.size];
    this.last = -1;
    this.count = 0;
  }

  /**
   * Clears the buffer, resetting count to 0.
   * All values are reset to 0.0.
   */
  public void clear() {
    for (int i = 0; i < buffer.length; i++) {
      buffer[i] = 0.0;
    }
    this.last = -1;
    this.count = 0;
  }

  /**
   * Returns the most recently added value.
   *
   * @return the latest value
   * @throws IllegalStateException if buffer is empty
   */
  public double getLatest() {
    if (count == 0) {
      throw new IllegalStateException("Buffer is empty");
    }
    return buffer[last];
  }

  /**
   * Returns the oldest value in the buffer (first to be overwritten).
   *
   * @return the oldest value
   * @throws IllegalStateException if buffer is empty
   */
  public double getOldest() {
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

  /**
   * Appends a value to the buffer, overwriting the oldest value if full.
   *
   * @param value the value to append
   * @return the old value that was overwritten, or 0.0 if buffer wasn't full
   */
  public double append(double value) {
    last += 1;
    if (last >= size)
      last = last - size;

    double oldValue = buffer[last];
    buffer[last] = value;

    if (count < size)
      count++;

    return oldValue;
  }

  /**
   * Returns the maximum capacity of the buffer.
   *
   * @return the buffer size
   */
  public final int size() {
    return size;
  }

  /**
   * Returns the number of elements actually added (0 to size).
   * Once full, this will always return size.
   *
   * @return the count of elements in the buffer
   */
  public int count() {
    return count;
  }

  /**
   * Returns true if the buffer has been filled to capacity at least once.
   *
   * @return true if buffer is full
   */
  public boolean isFull() {
    return count >= size;
  }

  /**
   * Returns the nth most recent item.
   *
   * @param n position from latest (1 = latest, 2 = second latest, etc.)
   * @return the value at position n
   * @throws IllegalArgumentException if n is invalid
   * @throws IndexOutOfBoundsException if n exceeds buffer size
   */
  public double getLatestNthItem(int n) {
    checkLatestNInput(n, n);

    int index = last - n + 1;
    if (index < 0)
      index += size;

    return buffer[index];
  }

  /**
   * Iterates over the latest n items in chronological order (oldest to newest).
   *
   * @param n number of items to iterate
   * @param consumer the consumer to accept each value
   * @throws IllegalArgumentException if n is invalid
   */
  public void forLatestNItems(int n, DoubleConsumer consumer) {
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

  /**
   * Copies the latest n values to the provided array in chronological order.
   *
   * @param dest destination array (must be at least n elements)
   * @param n number of values to copy
   * @throws IllegalArgumentException if n is invalid or dest is too small
   */
  public void copyLatestN(double[] dest, int n) {
    if (dest == null) {
      throw new IllegalArgumentException("Destination array cannot be null");
    }
    if (dest.length < n) {
      throw new IllegalArgumentException("Destination array too small: " + dest.length + " < " + n);
    }

    checkLatestNInput(n, n);

    int startIndex = last - n + 1;
    if (startIndex < 0)
      startIndex += size;

    for (int i = 0; i < n; i++) {
      int index = startIndex + i;
      if (index >= size)
        index -= size;
      dest[i] = buffer[index];
    }
  }

  private void checkLatestNInput(int n, int length) {
    if (n <= 0)
      throw new IllegalArgumentException(
              "n must be greater than 0. n=1 means \"the latest\", n=2 means \"the second latest\"");

    if (n > count)
      throw new IndexOutOfBoundsException("Only " + count + " elements in buffer, but wanted " + n + " items");

    if (length <= 0)
      throw new IllegalArgumentException("length must be greater than 0");

    if (n < length)
      throw new IndexOutOfBoundsException("length must be less than or equal to n");
  }
}
