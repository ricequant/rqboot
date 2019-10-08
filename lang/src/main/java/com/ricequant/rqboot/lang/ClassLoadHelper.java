package com.ricequant.rqboot.lang;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * @author chenfeng
 */
public class ClassLoadHelper {

  public static <T> Class<T> findImplWithClassLoader(String name, Class<T> targetCls, ClassLoader cl) {
    Class<?> clazz;
    try {
      clazz = cl.loadClass(name);
      if (clazz == null || clazz.isLocalClass() || clazz.isMemberClass() || clazz.isInterface() || clazz.isAnnotation()
              || clazz.isAnonymousClass() || clazz.isSynthetic()) {
        return null;
      }
    }
    catch (Throwable e) {
      return null;
    }

    Class<?>[] interfaces = clazz.getInterfaces();
    for (Class<?> interFace : interfaces) {
      if (targetCls.isAssignableFrom(interFace)) {
        //noinspection unchecked
        return (Class<T>) clazz;
      }
    }
    return null;
  }


  /**
   * Find all implementer of IMLModule within the process's classpath recursively
   *
   * @return list of classes found
   */
  public static <T> List<Class<T>> findImpInPathWithURLClassLoader(Class<T> clazz, IClassNameFilter nameFilter,
          URLClassLoader classLoader, URL[] searchUrls) {
    URL[] urls;
    if (searchUrls != null)
      urls = searchUrls;
    else
      urls = classLoader.getURLs();
    String[] pathList = new String[urls.length];
    int idx = 0;
    for (URL url : urls) {
      String path = url.getPath();
      pathList[idx++] = path;
    }

    return findImplFromFiles(pathList, clazz, nameFilter, classLoader);
  }

  public static <T> List<Class<T>> findImplInClassPath(Class<T> clazz, IClassNameFilter nameFilter) {
    String[] classPathList = System.getProperty("java.class.path").split(File.pathSeparator);
    return findImplFromFiles(classPathList, clazz, nameFilter, null);
  }

  private static <T> List<Class<T>> findImplFromFiles(String[] pathList, Class<T> clazz, IClassNameFilter nameFilter,
          ClassLoader classloader) {
    return doFindImplFromFiles(pathList, clazz, nameFilter, classloader);
  }

  private static <T> List<Class<T>> doFindImplFromFiles(String[] pathList, Class<T> clazz, IClassNameFilter nameFilter,
          ClassLoader classLoader) {
    List<File> fileList = new ArrayList<>();
    for (String pathEntry : pathList) {
      File pathEntryFile = new File(pathEntry);
      if (pathEntryFile.exists() && (pathEntry.endsWith(".jar") || pathEntry.endsWith(".class") || pathEntryFile
              .isDirectory())) {
        fileList.add(pathEntryFile);
      }
    }

    Queue<File> fileQueue = new LinkedList<>();
    fileQueue.addAll(fileList);

    List<Class<T>> impls = new ArrayList<>();
    while (!fileQueue.isEmpty()) {
      File file = fileQueue.poll();
      if (file.getName().endsWith(".jar")) {
        try {
          if (classLoader == null)
            classLoader = ClassLoader.getSystemClassLoader();

          JarInputStream is = new JarInputStream(new FileInputStream(file));

          JarEntry entry;
          while ((entry = is.getNextJarEntry()) != null) {
            if (entry.getName().endsWith(".class")) {
              tryAddImplFromClassFile(entry.getName(), impls, clazz, nameFilter, classLoader);
            }
          }
        }
        catch (Exception e) {
          // continue
        }
      }
      else if (file.getName().endsWith(".class")) {
        String classFilePath = file.getAbsolutePath();
        for (String originalRawPath : pathList) {
          if (classFilePath.startsWith(originalRawPath)) {
            classFilePath = classFilePath.substring(originalRawPath.length());
            if (classFilePath.startsWith(File.separator)) {
              classFilePath = classFilePath.substring(1);
            }
            tryAddImplFromClassFile(classFilePath, impls, clazz, nameFilter, classLoader);
          }
        }
      }
      else if (file.isDirectory()) {
        File[] subFiles = file.listFiles();
        if (subFiles != null) {
          for (File subFile : subFiles) {
            if (subFile.isDirectory() || subFile.getName().endsWith(".class") || subFile.getName().endsWith(".jar")) {
              fileQueue.offer(subFile);
            }
          }
        }
      }
    }
    return impls;
  }

  private static <T> void tryAddImplFromClassFile(String classFilePath, List<Class<T>> implClassList, Class<T> clazz,
          IClassNameFilter nameFilter, ClassLoader classLoader) {
    Class<T> impl = tryLoadClass(classFilePath, clazz, nameFilter, classLoader);
    if (impl != null) {
      implClassList.add(impl);
    }
  }

  /**
   * Try to load class which implements targetClass or which ever interface that extends targetClass in the .class file
   *
   * @param classFilePath
   *         path to the .class file
   * @param targetClazz
   *         Class instance of the class to load
   *
   * @return null if cannot find valid class, the class (not instance of the class) if it finds one
   */
  @SuppressWarnings("unchecked")
  private static <T> Class<T> tryLoadClass(String classFilePath, Class<T> targetClazz, IClassNameFilter nameFilter,
          ClassLoader classLoader) {
    Class<?> clazz;
    try {
      String className =
              classFilePath.substring(0, classFilePath.length() - ".class".length()).replace(File.separator, ".");
      if (!nameFilter.shouldLoad(className)) {
        return null;
      }
      clazz = classLoader.loadClass(className);
      if (clazz == null || clazz.isLocalClass() || clazz.isMemberClass() || clazz.isInterface() || clazz.isAnnotation()
              || clazz.isAnonymousClass() || clazz.isSynthetic()) {
        return null;
      }
    }
    catch (Throwable e) {
      return null;
    }

    Class<?>[] interfaces = clazz.getInterfaces();
    for (Class<?> interFace : interfaces) {
      if (targetClazz.isAssignableFrom(interFace)) {
        return (Class<T>) clazz;
      }
    }
    return null;
  }

  public interface IClassNameFilter {

    boolean shouldLoad(String name);
  }
}
