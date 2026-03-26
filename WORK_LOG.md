# Work Log

## 2026-03-26

- Added a new `management-server` module to expose an HTTP management API alongside the existing JMX path.
- Added default `mgmt-host`, `mgmt-port`, and `mgmt-token` arguments to rqboot's default command-line options.
- Wired `RicequantMain` to start and stop the embedded management server without changing the public rqboot API.
- Exposed `/management/info`, `/management/state`, `/management/log-level`, and `/management/shutdown` with static-token auth.
- Added runtime lifecycle tracking, current log-level reporting, JVM metrics collection, and management protocol documentation.
- Added parser and management-server tests and verified the repository with `mvn -q test`.
