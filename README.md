# rqboot

`rqboot` is a Java 21 bootstrap framework for RiceQuant services. It provides command-line/config bootstrapping, logging, JMX support, and an HTTP management server.

## Management Server

The HTTP management server exposes:

- process info and runtime state
- log-level changes
- graceful shutdown
- user-defined custom commands

Custom commands are the extension point used by `management-hub` and other operators to invoke service-specific actions through a typed JSON API.

See [management-server/README.md](management-server/README.md) for:

- endpoint details
- how to register custom commands
- supported argument types
- example code
