package com.ricequant.rqboot.config.cmd;

import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

/**
 * @author chenfeng
 */
public class LogPrintWriter extends PrintWriter {

  public LogPrintWriter(Logger logger) {
    super(new LogOutputStream(logger));
  }

  private static class LogOutputStream extends OutputStream {

    private Logger iLogger;

    private ByteArrayOutputStream iCache = new ByteArrayOutputStream();

    private List<Integer> iEndLineCache = new LinkedList<>();

    public LogOutputStream(Logger logger) {
      iLogger = logger;
    }

    @Override
    public void write(int b) throws IOException {
      byte[] sepBytes = System.lineSeparator().getBytes();
      if (iEndLineCache.size() == sepBytes.length)
        iEndLineCache.remove(0);

      iEndLineCache.add(b);
      iCache.write(b);

      boolean isEndLine = true;
      for (int i = 0; i < sepBytes.length; i++) {
        if (iEndLineCache.get(i) != sepBytes[i]) {
          isEndLine = false;
          break;
        }
      }

      if (isEndLine) {
        String line = iCache.toString();
        iLogger.info(line.substring(0, line.lastIndexOf(System.lineSeparator())));
        iCache.reset();
      }
    }
  }
}
