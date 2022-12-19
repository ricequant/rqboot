package com.ricequant.rqboot.lang;

import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class NativeHelper {

  private static final Set<String> loaded = new HashSet<>();

  /**
   * The minimum length a prefix for a file has to have according to {@link File#createTempFile(String, String)}}.
   */
  private static final int MIN_PREFIX_LENGTH = 3;

  public static final String NATIVE_FOLDER_PATH_PREFIX = "nativeutils";

  /**
   * Private constructor - this class will never be instanced
   */
  private NativeHelper() {
  }

  public static void loadLibraryFromResources(String... names) throws IOException {
    List<File> tempFiles = new ArrayList<>();
    for (var name : names) {
      if (loaded.contains(name))
        continue;
      String fileName = name;
      if (SystemUtils.IS_OS_LINUX)
        fileName = name + ".so";
      else if (SystemUtils.IS_OS_WINDOWS)
        fileName = name + ".dll";
      else if (SystemUtils.IS_OS_MAC_OSX) {
        fileName = name + ".dylib";
        if (NativeHelper.class.getClassLoader().getResource(fileName) == null)
          fileName = name + ".a";
      }

      if (NativeHelper.class.getClassLoader().getResource(fileName) == null)
        fileName = "lib" + fileName;
      InputStream resource = NativeHelper.class.getClassLoader().getResourceAsStream(fileName);

      File tempFile = null;
      try (resource) {
        if (resource == null) {
          throw new MissingResourceException("library not found", NativeHelper.class.getName(), fileName);
        }

        tempFile = FileHelper.createTempFile(NATIVE_FOLDER_PATH_PREFIX, fileName);
        FileHelper.copyStreamToTemp(resource, tempFile);
        tempFiles.add(tempFile);
      }
      catch (Throwable t) {
        if (tempFile != null)
          FileHelper.safeDeleteFile(tempFile);
        throw new IOException("Unable to create file", t);
      }
    }

    try {
      for (File f : tempFiles)
        FileHelper.loadPath(f);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    finally {
      for (File f : tempFiles)
        FileHelper.safeDeleteFile(f);
    }

    loaded.addAll(Arrays.asList(names));
  }

  public static List<String> resolveToLibFileNames(String... libNames) {
    List<String> ret = new ArrayList<>();
    for (String name : libNames) {
      String fileName = name;
      if (SystemUtils.IS_OS_LINUX)
        fileName = name + ".so";
      else if (SystemUtils.IS_OS_WINDOWS)
        fileName = name + ".dll";
      else if (SystemUtils.IS_OS_MAC_OSX) {
        fileName = name + ".dylib";
        if (NativeHelper.class.getClassLoader().getResource(fileName) == null)
          fileName = name + ".a";
      }
      ret.add(fileName);
    }
    return ret;
  }


  /**
   * Loads library from current JAR archive
   * <p>
   * The file from JAR is copied into system temporary directory and then loaded. The temporary file is deleted after
   * exiting. Method uses String as filename because the pathname is "abstract", not system-dependent.
   *
   * @param path The path of file inside JAR as absolute path (beginning with '/'), e.g. /package/File.ext
   * @throws IOException              If temporary file creation or read/write operation fails
   * @throws IllegalArgumentException If source file (param path) does not exist
   * @throws IllegalArgumentException If the path is not absolute or if the filename is shorter than three characters
   *                                  (restriction of {@link File#createTempFile(java.lang.String, java.lang.String)}).
   * @throws FileNotFoundException    If the file could not be found inside the JAR.
   */
  public static void loadLibraryFromJar(String path) throws IOException {

    if (null == path || !path.startsWith(File.separator)) {
      throw new IllegalArgumentException("The path has to be absolute (start with '/').");
    }

    // Obtain filename from path
    String[] parts = path.split(File.separator);
    String filename = (parts.length > 1) ? parts[parts.length - 1] : null;

    // Check if the filename is okay
    if (filename == null || filename.length() < MIN_PREFIX_LENGTH) {
      throw new IllegalArgumentException("The filename has to be at least 3 characters long.");
    }

    File temp = FileHelper.createTempFile(NATIVE_FOLDER_PATH_PREFIX, filename);
    InputStream is = NativeHelper.class.getResourceAsStream(path);
    if (is == null)
      throw new FileNotFoundException("File " + path + " was not found inside JAR.");

    FileHelper.copyStreamToTemp(is, temp);
  }


}