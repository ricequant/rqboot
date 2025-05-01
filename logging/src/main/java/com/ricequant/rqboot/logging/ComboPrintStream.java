package com.ricequant.rqboot.logging;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * A PrintStream implementation that writes to both a file and stdout simultaneously.
 * Can be used with System.setOut() to redirect console output to both console and file.
 * 
 * @author kangol
 */
public class ComboPrintStream extends PrintStream {

  private final PrintStream originalOut;

  /**
   * Creates a new ComboPrintStream that writes to both the specified file output stream and the original stdout.
   *
   * @param fos The file output stream to write to
   */
  public ComboPrintStream(FileOutputStream fos) {
    super(fos, true); // Create a PrintStream with auto-flush enabled
    this.originalOut = System.out; // Store the original stdout
  }

  /**
   * Creates a new ComboPrintStream that writes to both the specified file output stream and the specified original output stream.
   *
   * @param fos The file output stream to write to
   * @param originalOut The original output stream to also write to
   */
  public ComboPrintStream(FileOutputStream fos, PrintStream originalOut) {
    super(fos, true); // Create a PrintStream with auto-flush enabled
    this.originalOut = originalOut; // Store the provided original output stream
  }

  @Override
  public void write(int b) {
    super.write(b); // Write to file
    originalOut.write(b); // Write to original stdout
  }

  @Override
  public void write(byte[] buf, int off, int len) {
    super.write(buf, off, len); // Write to file
    originalOut.write(buf, off, len); // Write to original stdout
  }

  @Override
  public void flush() {
    super.flush(); // Flush file output
    originalOut.flush(); // Flush stdout
  }

  @Override
  public void close() {
    super.close(); // Close file output
    // Don't close the original stdout as it might be needed elsewhere
  }
}
