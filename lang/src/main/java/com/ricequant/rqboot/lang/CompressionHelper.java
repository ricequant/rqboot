package com.ricequant.rqboot.lang;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * @author chenfeng
 */
public class CompressionHelper {

  private static final int BUFFER_SIZE = 1024;

  public static byte[] compress(byte[] data) {
    Deflater deflater = new Deflater();
    deflater.setInput(data);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
    deflater.finish();
    byte[] buffer = new byte[BUFFER_SIZE];
    while (!deflater.finished()) {
      int count = deflater.deflate(buffer);
      outputStream.write(buffer, 0, count);
    }
    try {
      outputStream.close();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    return outputStream.toByteArray();
  }

  public static byte[] decompress(byte[] data) {
    Inflater inflater = new Inflater();
    inflater.setInput(data);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
    byte[] buffer = new byte[BUFFER_SIZE];
    try {
      while (!inflater.finished()) {
        int count = inflater.inflate(buffer);
        outputStream.write(buffer, 0, count);
      }
    }
    catch (DataFormatException e) {
      throw new RuntimeException("Input data format is wrong", e);
    }

    try {
      outputStream.close();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    return outputStream.toByteArray();
  }
}


