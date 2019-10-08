package com.ricequant.rqboot.lang;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author chenfeng
 */
public class FileHelper {

  public static InputStream resolveFileToStream(String configDir, String filePath, ClassLoader classLoader) {
    try {
      File file = new File(filePath);
      if (file.exists())
        return new FileInputStream(file);

      InputStream classPathStream = classLoader.getResourceAsStream(filePath);
      if (classPathStream != null)
        return classPathStream;

      String internalConfigDir = configDir;
      // set to current working directory if it's not set.
      if (internalConfigDir == null)
        internalConfigDir = new File(filePath).getParentFile().getAbsolutePath();

      if (!internalConfigDir.endsWith(File.separator))
        file = new File(internalConfigDir + File.separator + filePath);
      else
        file = new File(internalConfigDir + filePath);

      if (file.exists())
        return new FileInputStream(file);

      return null;
    }
    catch (Exception e) {
      return null;
    }
  }

  public static URL resolveFileToURL(String filePath, ClassLoader classLoader) {
    URL inClassPathURL = classLoader.getResource(filePath);
    if (inClassPathURL != null)
      return inClassPathURL;

    String relativeFilePath = resolveFilePath(filePath, classLoader);
    try {
      if (relativeFilePath != null)
        return new File(relativeFilePath).toURI().toURL();
      return null;
    }
    catch (MalformedURLException e) {
      return null;
    }
  }

  public static String resolveFilePath(String filePath, ClassLoader classLoader) {
    if (classLoader != null && classLoader.getResourceAsStream(filePath) != null) {
      return null;
    }

    File file = new File(filePath);
    if (file.exists())
      return file.getAbsolutePath();

    return null;
  }

  public static String resolveFileDir(String filePath, ClassLoader classLoader) {
    if (classLoader != null && classLoader.getResourceAsStream(filePath) != null) {
      return null;
    }

    File file = new File(filePath);
    if (file.exists())
      return file.getParentFile().getAbsolutePath();

    return null;
  }
}
