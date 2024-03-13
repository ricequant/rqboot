package com.ricequant.rqboot.lang;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;

/**
 * @author chenfeng
 */
public class FileHelper {

  private static File temporaryDir;

  public static File createTempFile(String subDir, String filename) throws IOException {
    // Prepare temporary file
    if (temporaryDir == null) {
      temporaryDir = createTempDirectory(subDir);
      temporaryDir.deleteOnExit();
    }
    return new File(temporaryDir, filename);
  }

  public static File temporaryDir() {
    return temporaryDir;
  }


  public static File createTempDirectory(String prefix) throws IOException {
    String tempDir = System.getProperty("java.io.tmpdir");
    File generatedDir = new File(tempDir, prefix + "_" + System.currentTimeMillis());

    if (generatedDir.exists() && generatedDir.isDirectory() && generatedDir.canWrite())
      return generatedDir;

    if (!generatedDir.mkdir())
      throw new IOException("Failed to create temp directory " + generatedDir.getName());

    return generatedDir;
  }

  public static List<File> copyResourceToPath(File targetPath, String... resourceNames) throws IOException {
    List<File> ret = new ArrayList<>();
    if (targetPath.exists() && targetPath.isDirectory()) {
      for (String resourceName : resourceNames) {
        InputStream resource = FileHelper.class.getClassLoader().getResourceAsStream(resourceName);
        if (resource == null)
          throw new MissingResourceException("resource not found: " + resourceName, NativeHelper.class.getName(),
                  resourceName);

        File targetFile = new File(targetPath, resourceName);
        try {
          copyStreamToTemp(resource, targetFile);
          ret.add(targetFile);
        }
        catch (IOException e) {
          safeDeleteFile(targetFile);
          throw e;
        }
      }
    }
    return ret;
  }


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

  public static void loadPath(File temp) {
    System.load(temp.getAbsolutePath());
  }

  public static void safeDeleteFile(File temp) {
    if (isPosixCompliant()) {
      // Assume POSIX compliant file system, can be deleted after loading
      temp.delete();
    }
    else {
      // Assume non-POSIX, and don't delete until last file descriptor closed
      temp.deleteOnExit();
    }
  }

  public static void copyStreamToTemp(InputStream is, File temp) throws IOException {
    try {
      System.out.println("Writing stream to path: " + temp.getAbsolutePath());
      Files.copy(is, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
    catch (IOException e) {
      temp.delete();
      throw e;
    }
  }

  public static boolean isPosixCompliant() {
    try {
      return FileSystems.getDefault().supportedFileAttributeViews().contains("posix");
    }
    catch (FileSystemNotFoundException | ProviderNotFoundException | SecurityException e) {
      return false;
    }
  }

}
