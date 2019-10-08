package com.ricequant.rqboot.lang;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * @author chenfeng
 */
public class CodecHelper {

  private static Charset UTF8_Codec = Charset.forName("UTF-8");

  public static String decodeUTF8String(ByteBuffer buffer, int offset, int length) {
    if (length <= 0)
      return "";

    byte b = buffer.array()[offset];
    int end = offset;
    while (b != '\0' && end < length + offset) {
      b = buffer.array()[end];
      end++;
    }

    if (end == offset)
      return "";

    return new String(buffer.array(), offset, end - offset - (b == '\0' ? 1 : 0), UTF8_Codec);
  }

  public static String decodeAsciiString(ByteBuffer buffer, int offset, int length) {
    if (length <= 0)
      return "";

    byte b = buffer.array()[offset];
    int end = offset;
    while (b != '\0' && end < length + offset) {
      b = buffer.array()[end];
      end++;
    }

    if (end == offset)
      return "";

    return new String(buffer.array(), offset, end - offset - (b == '\0' ? 1 : 0));
  }

  public static int encodeUTF8String(ByteBuffer buffer, String str, int offset, int maxLength) {
    byte[] bytes = str.getBytes(UTF8_Codec);
    int length = maxLength > bytes.length ? bytes.length : maxLength;

    int originalOffset = buffer.position();
    Arrays.fill(buffer.array(), offset, offset + length, (byte) 0);
    buffer.position(offset);
    buffer.put(bytes, 0, length);
    buffer.position(originalOffset);

    return length;
  }

  public static int encodeAsciiString(ByteBuffer buffer, String str, int offset, int maxLength) {
    byte[] bytes = str.getBytes();
    int length = maxLength > bytes.length ? bytes.length : maxLength;

    int originalOffset = buffer.position();
    Arrays.fill(buffer.array(), offset, offset + length, (byte) 0);
    buffer.position(offset);
    buffer.put(bytes, 0, length);
    buffer.position(originalOffset);

    return length;
  }

  public static String escapeQuotes(Object src) {
    return src.toString().replace("\\", "\\\\").replace("\n", "\\n").replace("\"", "\\\"");
  }

  public static void escapeStringArray(StringBuilder sb, Object[] array) {
    sb.append("[");
    for (int i = 0; i < array.length; i++) {
      Object s = array[i];
      sb.append("\"");
      sb.append(escapeQuotes(s));
      sb.append("\"");
      if (i < array.length - 1)
        sb.append(",");
    }
    sb.append("]");
  }
}
