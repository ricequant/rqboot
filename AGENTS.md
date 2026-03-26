# Repository Guidelines

## Project Structure & Module Organization
`rqboot` is a Maven multi-module Java 21 project. Root modules are `lang/`, `logging/`, `config/`, `jmx/`, and `boot/`.

- `lang/`: shared utility code and the only current test tree at `lang/src/test/java`.
- `logging/`: Log4j integration and JMX-backed logging controls.
- `config/`: command-line/config parsing plus the XSD in `config/src/main/resources/config-types.xsd`.
- `jmx/`: parent module for `jmx-server`, `jmx-client`, `jmx-shared-resource`, and `jmx-cmd`.
- `boot/`: application bootstrap classes such as `com.ricequant.rqboot.boot.RicequantMain`.

Keep handwritten code in `src/main/java` under `com.ricequant.rqboot.*`. Do not edit generated files under `*/target/`, especially `config/target/generated-sources/config`.

## Build, Test, and Development Commands
- `mvn test`: run all module tests from the repo root; this also triggers schema code generation in `config`.
- `mvn clean verify`: full clean build, compile, test, and package across modules.
- `mvn -pl lang test`: run only the `lang` module tests.
- `mvn -pl boot -am package`: build `boot` and any required upstream modules.
- `mvn -pl jmx/jmx-cmd -am package`: package the JMX command-line tool and its dependencies.

## Coding Style & Naming Conventions
Follow the existing style in each file instead of reformatting broadly. Java code uses same-line braces and standard package naming. Use `UpperCamelCase` for classes, `lowerCamelCase` for methods and fields, and keep the existing `I...` interface prefix only where the module already uses it, such as `IApplication`.

No Spotless or Checkstyle configuration is committed. Use your IDE formatter conservatively. Existing POM files are tab-indented; Java files use spaces.

## Testing Guidelines
Place tests in `*/src/test/java` and name them `*Test.java`. The repository uses JUnit 5 (`org.junit.jupiter`). Add focused tests for new public behavior, especially in utility, config, and JMX code. There is no enforced coverage threshold, so rely on meaningful assertions and run `mvn test` before submitting changes.

## Commit & Pull Request Guidelines
Recent commits use short, imperative, lowercase subjects, for example `add tests for key DateTimeHelper class` and `refactor time processing into DateTimeHelper`. Keep each commit scoped to one change.

Pull requests should list touched modules, describe behavior changes, note any schema or config impact, and include the Maven commands used for verification. For CLI-facing changes, include a short example of the new output or invocation.
