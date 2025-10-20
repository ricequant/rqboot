# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

rqboot is a Java 21 multi-module Maven project providing a bootstrapping framework for Ricequant applications. It includes utilities for application lifecycle management, JMX monitoring, configuration management, and common helper functions.

## Build and Test Commands

### Build
```bash
mvn clean install
```

### Build without tests
```bash
mvn clean install -DskipTests
```

### Run tests
```bash
mvn test
```

### Run tests for a specific module
```bash
mvn test -pl <module-name>
# Example: mvn test -pl lang
```

### Generate sources (for config module with JAXB)
```bash
mvn generate-sources
```

### Check for dependency updates
```bash
mvn versions:display-dependency-updates
```

## Module Architecture

### Core Modules (in dependency order)

1. **lang** - Core utilities and helpers
   - Contains helper classes like `DateTimeHelper`, `StringHelper`, `ArrayHelper`, etc.
   - Provides factory patterns (`GetOrCreateFactory`, `ExternalMapGetOrCreateFactory`)
   - Buffer implementations (`ObjectRingBuffer`)
   - Lambda utilities for primitives (`IntBiConsumer`, `LongBiConsumer`)

2. **logging** - Logging configuration
   - Log4j2-based logging setup with SLF4J facade
   - `LogConfiguration` for runtime log level management
   - `LogLevelJmx` for JMX-based log control
   - `ComboPrintStream` for dual-output streams

3. **config** - Configuration management
   - Uses JAXB to generate config classes from XML schemas (config-types.xsd)
   - `RQConfig`, `RQGlobal`, `RQProcessConfig` for JSON-based configuration
   - Command-line argument parsing via `CommandLineParser` and `CommandLineArgs`
   - Endpoint, MongoDB, MySQL, Redis, and HTTP server configuration types
   - Generated sources in `target/generated-sources/config`

4. **jmx** - JMX monitoring (multi-module)
   - **jmx-shared-resource**: Common annotations (`@JmxBean`, `@JmxMethod`, `@JmxParam`)
   - **jmx-server**: `JmxBeanRegistry` for registering MBeans dynamically
   - **jmx-client**: `JmxCommandExecutor` for remote JMX invocation
   - **jmx-cmd**: Command-line tool for JMX operations

5. **boot** - Application bootstrapping
   - `RicequantMain`: Main entry point with lifecycle management
   - `IApplication`: Interface for application implementations
   - `VMStatsReporter`: JVM statistics reporter
   - Automatic discovery of `IApplication` implementations via classpath scanning
   - Shutdown hook support via `IDaemonCallback`

## Key Design Patterns

### Application Lifecycle
Applications extend `IApplication` and implement:
- `customizeArgs()`: Define command-line arguments
- `init()`: Initialize application with parsed arguments
- `start()`: Start application services with JMX registry
- `tearDown()`: Cleanup on shutdown

The framework automatically:
1. Discovers `IApplication` implementations in classpath
2. Parses command-line arguments
3. Configures logging
4. Starts JMX server (unless `--no-jmx` specified)
5. Manages shutdown hooks

### JMX Integration
Beans annotated with `@JmxBean` are automatically registered. Methods annotated with `@JmxMethod` become remotely invocable:
```java
@JmxBean(description = "My Service")
public class MyService {
    @JmxMethod(description = "Do something")
    public String doSomething(@JmxParam(name = "input") String input) {
        return "result";
    }
}
```

Register via: `jmxBeanRegistry.register(myServiceInstance)`

### Configuration Management
Configurations are loaded from:
- XML schemas (JAXB-generated classes)
- JSON files (FastJSON parsing)
- Command-line arguments (Commons CLI)

### Time Handling
The codebase uses custom timestamp formats:
- **RQ Timestamp**: Microseconds since epoch (long)
- **Readable Timestamp**: YYYYMMDDHHMMSSmmm format (17 digits)
- **Int Date**: YYYYMMDD format (e.g., 20231015)
- **Int Time**: HHMMSSmmm format (e.g., 143020000 for 14:30:20.000)

Use `DateTimeHelper` utilities for conversions between formats.

## Common Command-Line Arguments

Standard arguments available to all applications (defined in `RicemapDefaultArgs`):
- `--cwd <path>`: Set working directory
- `--debug-level <level>`: Set logging level
- `--redirect-console`: Redirect console output
- `--no-jmx`: Disable JMX server
- `--jmx-host <host>`: JMX server host (default: localhost)
- `--jmx-port <port>`: JMX server port (0 = auto-assign)
- `--instance-name <name>`: Process instance name
- `-v, --version`: Print version information

## Special Considerations

### JAXB Schema Generation
The config module uses JAXB to generate Java classes from XSD schemas. If you modify `config-types.xsd`, regenerate with:
```bash
mvn generate-sources -pl config
```

### Git Properties
Maven automatically embeds git information in JARs via `git-commit-id-maven-plugin`. The `git.properties` file includes:
- Branch name
- Commit ID (full and abbreviated)
- Commit time
- Tags
- Build time

### Thread-Local Calendars
`DateTimeHelper` uses thread-local `Calendar` instances set to Asia/Shanghai timezone for performance. Be aware of timezone implications.

### JMX URL Files
JMX server writes connection URL to temp directory as `<process-name>.jmx` for client discovery.

## Dependencies

Key libraries used:
- Apache Commons (Lang3, IO, CLI)
- Joda-Time (legacy date/time handling, being migrated to java.time)
- Log4j2 + SLF4J
- FastJSON (Alibaba)
- JAXB Runtime
- JUnit Jupiter (testing)

## Version and Artifact Information

- Group ID: `com.ricequant`
- Artifact ID: `rqboot`
- Version: `1.0-SNAPSHOT`
- Java Version: 21
- Maven Version: 3.9.11+
