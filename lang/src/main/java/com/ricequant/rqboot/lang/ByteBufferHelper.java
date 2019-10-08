package com.ricequant.rqboot.lang;

import org.apache.commons.lang3.StringUtils;

import java.nio.ByteBuffer;

/**
 * @author chenfeng
 */
public class ByteBufferHelper {

  public static void byteBufferCopy(ByteBuffer origBuffer, ByteBuffer newBuffer) {
    System.arraycopy(origBuffer.array(), 0, newBuffer.array(), 0,
            Math.min(origBuffer.capacity(), newBuffer.capacity()));

    newBuffer.position(Math.min(newBuffer.capacity(), origBuffer.position()));
    newBuffer.limit(Math.min(origBuffer.limit(), newBuffer.capacity()));
    newBuffer.order(origBuffer.order());
  }


  /**
   * Create a new ByteBuffer, from 0 until limit and copies the data. It will not mimic the actual capacity, but copy
   * "what is needed".
   *
   * @param origBuffer
   *         Byte buffer to copy.
   *
   * @return A copy
   */
  public static ByteBuffer byteBufferCopy(ByteBuffer origBuffer) {
    return byteBufferCopy(origBuffer, 0, origBuffer.capacity());
  }

  /**
   * Returns a copy of the contents of a ByteBuffer.
   * <p>
   * The new ByteBuffer is only as big as needed for the copy.
   *
   * @param origBuffer
   *         The ByteBuffer to copy
   * @param position
   *         Start position.
   * @param length
   *         How long.
   *
   * @return A copy
   */
  public static ByteBuffer byteBufferCopy(ByteBuffer origBuffer, int position, int length) {

    byte[] from = origBuffer.array();
    byte[] to = new byte[length];
    System.arraycopy(from, position, to, 0, length);
    ByteBuffer newBuffer = ByteBuffer.wrap(to);
    newBuffer.position(origBuffer.position());
    newBuffer.limit(origBuffer.limit());
    newBuffer.order(origBuffer.order());

    return newBuffer;
  }

  /**
   * Converts the content of a ByteBuffer to a string of hex values.
   *
   * @param bb
   *         The ByteBuffer to convert
   * @param wrap
   *         Set to true, for line breaks in print out.
   *
   * @return The converted string
   */
  public static String byteBuffer2Hex(ByteBuffer bb, boolean wrap) {

    int p = bb.position();
    byte[] b = new byte[bb.remaining()];
    bb.get(b);
    bb.position(p);

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < b.length; i++) {
      sb.append(String.format("%02x ", b[i]));
      if (wrap && (i & 15) == 15)
        sb.append("\n");
    }

    return sb.toString();
  }


  /**
   * Converts all the content of a ByteBuffer to a string of hex values.
   *
   * @param bb
   *         The ByteBuffer to convert
   * @param wrap
   *         Set to true, for line breaks in print out.
   *
   * @return The converted string
   */
  public static String fullByteBuffer2Hex(ByteBuffer bb, boolean wrap) {

    int p = bb.position();
    int l = bb.limit();
    bb.position(0);
    bb.limit(bb.capacity());

    byte[] b = new byte[bb.remaining()];
    bb.get(b);
    bb.position(p);
    bb.limit(l);

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < b.length; i++) {
      sb.append(String.format("%02x ", b[i]));
      if (wrap && (i & 15) == 15)
        sb.append("\n");
    }

    return sb.toString();
  }

  public static byte[] hexStringToByteArray(String in) {
    String s = StringUtils.trim(in);
    s = String.join("", s.split("[ \n]+"));
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
    }
    return data;
  }
}
