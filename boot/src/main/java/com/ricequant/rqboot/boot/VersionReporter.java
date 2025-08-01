package com.ricequant.rqboot.boot;

import com.ricequant.rqboot.jmx.shared_resource.annotations.JmxBean;
import com.ricequant.rqboot.jmx.shared_resource.annotations.JmxMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * @author kangol
 */
@JmxBean("VersionReporter")
public class VersionReporter {

    private static final Logger logger = LoggerFactory.getLogger(VersionReporter.class);

    /**
     * Cache for discovered libraries to avoid repeated scanning
     */
    private static volatile List<LibraryInfo> cachedLibraries = null;

    /**
     * Print version information for discovered libraries
     */
    public static void printVersionInfo() {
        logger.info("=== RQAlphaJ Version Information ===");
        
        VersionReporter reporter = new VersionReporter();
        List<LibraryInfo> libraries = reporter.getDiscoveredLibraries();
        
        for (LibraryInfo library : libraries) {
            logger.info("{}: version={}, build={}, git={}, jar={}", 
                       library.name, 
                       library.version != null ? library.version : "unknown",
                       library.buildTime != null ? library.buildTime : "unknown",
                       library.gitInfo != null ? library.gitInfo : "unknown",
                       library.jarPath != null ? library.jarPath : "unknown");
        }
        
        logger.info("=== End Version Information ===");
    }

    /**
     * Get version information for discovered libraries (JMX method)
     */
    @JmxMethod("getVersionInfo")
    public List<String> getVersionInfo() {
        List<String> result = new ArrayList<>();
        List<LibraryInfo> libraries = getDiscoveredLibraries();
        
        for (LibraryInfo library : libraries) {
            result.add(String.format("%s: version=%s, build=%s, git=%s", 
                      library.name, 
                      library.version != null ? library.version : "unknown",
                      library.buildTime != null ? library.buildTime : "unknown",
                      library.gitInfo != null ? library.gitInfo : "unknown"));
        }
        
        return result;
    }

    /**
     * Get discovered libraries using cached result for performance
     */
    public List<LibraryInfo> getDiscoveredLibraries() {
        if (cachedLibraries == null) {
            synchronized (VersionReporter.class) {
                if (cachedLibraries == null) {
                    cachedLibraries = discoverFromClasspath();
                }
            }
        }
        return new ArrayList<>(cachedLibraries);
    }
    
    /**
     * Discover ricequant libraries from system classpath (cleanest approach)
     */
    private List<LibraryInfo> discoverFromClasspath() {
        List<LibraryInfo> discovered = new ArrayList<>();
        
        String classpath = System.getProperty("java.class.path");
        if (classpath != null) {
            for (String path : classpath.split(System.getProperty("path.separator"))) {
                if (path.endsWith(".jar") && path.contains("ricequant")) {
                    LibraryInfo library = getLibraryInfoFromJar(path);
                    if (library != null) {
                        discovered.add(library);
                    }
                }
            }
        }
        
        // Add essential non-ricequant libraries
        addEssentialLibraries(discovered);
        
        return discovered;
    }
    
    /**
     * Extract library information directly from JAR manifest
     */
    private LibraryInfo getLibraryInfoFromJar(String jarPath) {
        try (JarFile jar = new JarFile(jarPath)) {
            Manifest manifest = jar.getManifest();
            if (manifest != null) {
                Attributes attrs = manifest.getMainAttributes();
                
                // Check if this is actually a ricequant JAR
                String groupId = attrs.getValue("GroupId");
                String vendor = attrs.getValue("Implementation-Vendor");
                String title = attrs.getValue("Implementation-Title");
                
                boolean isRicequant = (groupId != null && groupId.contains("ricequant")) ||
                                     (vendor != null && vendor.contains("RiceQuant")) ||
                                     (title != null && title.contains("ricequant")) ||
                                     jarPath.contains("ricequant");
                                     
                if (isRicequant) {
                    String libraryName = getLibraryNameFromManifest(attrs, jarPath);
                    String version = getVersionFromManifest(attrs);
                    String buildTime = getBuildTimeFromManifest(attrs);
                    String gitInfo = getGitInfoFromManifest(attrs);
                    
                    return new LibraryInfo(libraryName, version, buildTime, gitInfo, jarPath);
                }
            }
        } catch (IOException e) {
            logger.debug("Could not read JAR manifest: {}", jarPath);
        }
        
        return null;
    }
    
    /**
     * Get library name from Maven manifest attributes
     */
    private String getLibraryNameFromManifest(Attributes attrs, String jarPath) {
        // Try multiple manifest attributes in order of preference
        String name = attrs.getValue("Implementation-Title");
        if (name != null && !name.trim().isEmpty()) {
            return name.trim();
        }
        
        name = attrs.getValue("Bundle-Name");
        if (name != null && !name.trim().isEmpty()) {
            return name.trim();
        }
        
        name = attrs.getValue("Specification-Title");
        if (name != null && !name.trim().isEmpty()) {
            return name.trim();
        }
        
        // Try artifact ID from Maven
        name = attrs.getValue("ArtifactId");
        if (name != null && !name.trim().isEmpty()) {
            return formatArtifactId(name.trim());
        }
        
        // Fallback: extract from JAR filename
        return getNameFromJarPath(jarPath);
    }
    
    /**
     * Get version from manifest attributes
     */
    private String getVersionFromManifest(Attributes attrs) {
        String version = attrs.getValue("Implementation-Version");
        if (version != null && !version.trim().isEmpty()) {
            return version.trim();
        }
        
        version = attrs.getValue("Bundle-Version");
        if (version != null && !version.trim().isEmpty()) {
            return version.trim();
        }
        
        version = attrs.getValue("Specification-Version");
        if (version != null && !version.trim().isEmpty()) {
            return version.trim();
        }
        
        return null;
    }
    
    /**
     * Get build time from manifest attributes
     */
    private String getBuildTimeFromManifest(Attributes attrs) {
        String buildTime = attrs.getValue("Build-Time");
        if (buildTime != null && !buildTime.trim().isEmpty()) {
            return buildTime.trim();
        }
        
        buildTime = attrs.getValue("Built-Date");
        if (buildTime != null && !buildTime.trim().isEmpty()) {
            return buildTime.trim();
        }
        
        buildTime = attrs.getValue("Build-Date");
        if (buildTime != null && !buildTime.trim().isEmpty()) {
            return buildTime.trim();
        }
        
        // Sometimes Maven puts build info in Created-By
        buildTime = attrs.getValue("Created-By");
        if (buildTime != null && buildTime.contains("Maven")) {
            return buildTime.trim();
        }
        
        return null;
    }
    
    /**
     * Get Git information from manifest attributes
     */
    private String getGitInfoFromManifest(Attributes attrs) {
        String branch = attrs.getValue("Git-Branch");
        String commit = attrs.getValue("Git-Commit");
        String commitTime = attrs.getValue("Git-Commit-Time");
        String tags = attrs.getValue("Git-Tags");
        
        if (branch == null && commit == null && commitTime == null && tags == null) {
            return null;
        }
        
        StringBuilder gitInfo = new StringBuilder();
        
        if (branch != null && !branch.trim().isEmpty()) {
            gitInfo.append(branch.trim());
        }
        
        if (commit != null && !commit.trim().isEmpty()) {
            if (gitInfo.length() > 0) gitInfo.append("@");
            gitInfo.append(commit.trim());
        }
        
        if (tags != null && !tags.trim().isEmpty() && !tags.equals("")) {
            if (gitInfo.length() > 0) gitInfo.append(" ");
            gitInfo.append("[").append(tags.trim()).append("]");
        }
        
        return gitInfo.length() > 0 ? gitInfo.toString() : null;
    }
    
    /**
     * Format artifact ID into a friendly display name
     */
    private String formatArtifactId(String artifactId) {
        // Convert kebab-case to Title Case
        String[] parts = artifactId.split("-");
        StringBuilder formatted = new StringBuilder();
        
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) formatted.append("-");
            String part = parts[i];
            if (part.length() > 0) {
                formatted.append(part.substring(0, 1).toUpperCase())
                         .append(part.substring(1).toLowerCase());
            }
        }
        
        return formatted.toString();
    }
    
    /**
     * Fallback: extract name from JAR path when manifest has no useful info
     */
    private String getNameFromJarPath(String jarPath) {
        String jarName = jarPath.substring(jarPath.lastIndexOf('/') + 1);
        
        // Remove version suffix and extension
        String baseName = jarName.replaceFirst("-[0-9].*\\.(jar|JAR)$", "");
        
        return formatArtifactId(baseName);
    }
    
    /**
     * Add essential non-ricequant libraries that should always be reported
     */
    private void addEssentialLibraries(List<LibraryInfo> discovered) {
        String[] essentialClasses = {
            "org.slf4j.Logger",
            "io.vertx.core.Vertx"
        };
        
        for (String className : essentialClasses) {
            LibraryVersion version = getLibraryVersion(className);
            LibraryInfo info = new LibraryInfo(
                version.name, 
                version.version, 
                version.buildTime,
                null,  // Git info not available for external libraries
                version.jarLocation
            );
            discovered.add(info);
        }
    }
    
    /**
     * Get version information for a specific library
     */
    public LibraryVersion getLibraryVersion(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            
            String libraryName = getLibraryName(className);
            Package pkg = clazz.getPackage();
            
            // Try to get version from package first
            String version = pkg != null ? pkg.getImplementationVersion() : null;
            String buildTime = null;
            String jarLocation = null;
            
            // Try to get more detailed info from JAR manifest
            try {
                URL location = clazz.getProtectionDomain().getCodeSource().getLocation();
                String path = location.getPath();
                jarLocation = path;
                
                if (path.endsWith(".jar")) {
                    try (JarFile jar = new JarFile(path)) {
                        Manifest manifest = jar.getManifest();
                        if (manifest != null) {
                            Attributes attrs = manifest.getMainAttributes();
                            
                            // Try multiple possible version attributes
                            if (version == null) {
                                version = attrs.getValue("Implementation-Version");
                            }
                            if (version == null) {
                                version = attrs.getValue("Bundle-Version");
                            }
                            if (version == null) {
                                version = attrs.getValue("Specification-Version");
                            }
                            
                            // Try multiple possible build time attributes
                            buildTime = attrs.getValue("Build-Time");
                            if (buildTime == null) {
                                buildTime = attrs.getValue("Built-Date");
                            }
                            if (buildTime == null) {
                                buildTime = attrs.getValue("Build-Date");
                            }
                            if (buildTime == null) {
                                buildTime = attrs.getValue("Created-By");
                            }
                        }
                    }
                } else {
                    // Not a JAR file - probably running from IDE/target/classes
                    jarLocation = "development";
                }
            } catch (Exception e) {
                logger.debug("Could not read JAR manifest for {}: {}", className, e.getMessage());
            }
            
            return new LibraryVersion(libraryName, version, buildTime, jarLocation);
            
        } catch (ClassNotFoundException e) {
            logger.debug("Library not found: {}", className);
            return new LibraryVersion(getLibraryName(className), null, null, "not found");
        }
    }

    /**
     * Extract a friendly library name from the class name
     */
    private String getLibraryName(String className) {
        if (className.contains("gdk")) return "GDK";
        if (className.contains("xtp") && className.contains("recorder")) return "XTP-MD-Recorder";
        if (className.contains("xtp")) return "XTP";
        if (className.contains("rqdataj")) return "RQDataJ";
        if (className.contains("rqmdj")) return "RQMDJ";
        if (className.contains("rqboot")) return "RQBoot";
        if (className.contains("slf4j")) return "SLF4J";
        if (className.contains("vertx")) return "Vert.x";
        
        // Extract simple class name as fallback
        return className.substring(className.lastIndexOf('.') + 1);
    }

    /**
     * Get dependency information from current JAR manifest (if available)
     */
    @JmxMethod("getDependencyInfo")
    public String getDependencyInfo() {
        try {
            Class<?> clazz = VersionReporter.class;
            URL location = clazz.getProtectionDomain().getCodeSource().getLocation();
            String path = location.getPath();
            
            if (path.endsWith(".jar")) {
                try (JarFile jar = new JarFile(path)) {
                    Manifest manifest = jar.getManifest();
                    if (manifest != null) {
                        Attributes attrs = manifest.getMainAttributes();
                        StringBuilder sb = new StringBuilder();
                        sb.append("Current JAR Dependencies:\n");
                        
                        // Look for common dependency-related manifest attributes
                        String classpath = attrs.getValue("Class-Path");
                        if (classpath != null) {
                            sb.append("Classpath: ").append(classpath).append("\n");
                        }
                        
                        String dependencies = attrs.getValue("Dependencies");
                        if (dependencies != null) {
                            sb.append("Dependencies: ").append(dependencies).append("\n");
                        }
                        
                        String buildDependencies = attrs.getValue("Build-Dependencies");
                        if (buildDependencies != null) {
                            sb.append("Build-Dependencies: ").append(buildDependencies).append("\n");
                        }
                        
                        if (sb.length() == "Current JAR Dependencies:\n".length()) {
                            sb.append("No dependency information found in manifest\n");
                        }
                        
                        return sb.toString();
                    } else {
                        return "No manifest found in JAR";
                    }
                }
            } else {
                return "Not running from JAR, dependency info not available";
            }
        } catch (Exception e) {
            return "Error reading dependency info: " + e.getMessage();
        }
    }

    /**
     * Test method to verify manifest reading works
     */
    @JmxMethod("testManifestReading")
    public String testManifestReading() {
        try {
            // Test with our own class
            Class<?> clazz = VersionReporter.class;
            URL location = clazz.getProtectionDomain().getCodeSource().getLocation();
            String path = location.getPath();
            
            if (path.endsWith(".jar")) {
                try (JarFile jar = new JarFile(path)) {
                    Manifest manifest = jar.getManifest();
                    if (manifest != null) {
                        Attributes attrs = manifest.getMainAttributes();
                        StringBuilder sb = new StringBuilder();
                        sb.append("JAR: ").append(path).append("\n");
                        sb.append("Manifest attributes:\n");
                        for (Object key : attrs.keySet()) {
                            sb.append("  ").append(key).append(": ").append(attrs.getValue(key.toString())).append("\n");
                        }
                        return sb.toString();
                    } else {
                        return "JAR found but no manifest: " + path;
                    }
                }
            } else {
                return "Not running from JAR: " + path;
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Container for library information discovered from JARs
     */
    public static class LibraryInfo {
        public final String name;
        public final String version;
        public final String buildTime;
        public final String gitInfo;
        public final String jarPath;

        public LibraryInfo(String name, String version, String buildTime, String gitInfo, String jarPath) {
            this.name = name;
            this.version = version;
            this.buildTime = buildTime;
            this.gitInfo = gitInfo;
            this.jarPath = jarPath;
        }
    }
    
    /**
     * Container for library version information (legacy support)
     */
    public static class LibraryVersion {
        public final String name;
        public final String version;
        public final String buildTime;
        public final String jarLocation;

        public LibraryVersion(String name, String version, String buildTime, String jarLocation) {
            this.name = name;
            this.version = version;
            this.buildTime = buildTime;
            this.jarLocation = jarLocation;
        }
    }
}
