# Management Protocol

All endpoints require the `X-Management-Token` header. `rqboot` defaults to `Ricequant123`; override it in real deployments.

## Endpoints

- `GET /management/info`
  Returns structured descriptive metadata for rendering:
  - `producerType`
  - `title`
  - `subtitle`
  - `tile`
  - `overview`
  - `sections`
  Each item or section carries display type information so hubs and UIs can render non-`RicequantMain` processes without hardcoded rqboot assumptions.

- `GET /management/state`
  Returns dynamic runtime state: lifecycle state, health, uptime, current debug level, and JVM memory/CPU/thread metrics.

- `POST /management/log-level`
  Request body: `{"level":"2"}`. Changes the active debug level immediately and returns the updated level.

- `POST /management/shutdown`
  Request body: `{"reason":"operator requested"}`. Initiates graceful shutdown through the existing `RicequantMain.stop()` path.

- `GET /management/commands`
  Returns the user-registered custom management commands. Each item includes command name, title, description, and typed argument metadata.

- `POST /management/commands/{commandName}`
  Executes a user-registered custom management command. The request body is a JSON object whose fields match the declared command arguments.
  Supported argument types are `long`, `double`, `string`, `long[]`, `double[]`, and `string[]`.

## Status Codes

- `200` success
- `400` malformed JSON or missing required fields
- `401` missing or invalid management token
- `405` wrong HTTP method
- `500` unexpected server error

## Polling Model

- Clients should fetch `/management/info` on page load or manual refresh.
- Clients should poll `/management/state` periodically.
- Force refresh from a hub should call both endpoints explicitly and store the fetch timestamp.
- Clients can fetch `/management/commands` when opening an operator command panel or when the process definition changes.

## Registering Custom Commands

Applications receive `ManagementCommandService` in `IApplication.start(...)`. Register commands there before the service is exposed to users.
The example below shows the registration pattern; replace `cacheService` with your own application service.

### Minimal Example

```java
import com.ricequant.rqboot.jmx.server.IJmxBeanRegistry;
import com.ricequant.rqboot.management.server.ManagementCommandArg;
import com.ricequant.rqboot.management.server.ManagementCommandArgType;
import com.ricequant.rqboot.management.server.ManagementCommandService;
import com.ricequant.rqboot.management.server.ManagementCustomCommand;

import java.util.List;
import java.util.Map;

@Override
public void start(IJmxBeanRegistry beanRegistry, ManagementCommandService managementCommandService) throws Exception {
  managementCommandService.registerCustomCommand(
          new ManagementCustomCommand("reloadCache", "Reload Cache", "Reload cache entries for one account.", List.of(
                  ManagementCommandArg.required("accountId", ManagementCommandArgType.STRING)
                          .titled("Account ID"),
                  ManagementCommandArg.optional("partitions", ManagementCommandArgType.LONG_ARRAY)
                          .titled("Partitions")
                          .describedAs("Optional partition ids to reload."))),
          args -> {
            String accountId = (String) args.get("accountId");
            long[] partitions = args.containsKey("partitions") ? (long[]) args.get("partitions") : new long[0];

            int reloaded = cacheService.reload(accountId, partitions);
            return Map.of(
                    "ok", true,
                    "accountId", accountId,
                    "reloadedEntries", reloaded);
          });

  // continue normal startup...
}
```

### Command Metadata

Each command defines:

- `name`: stable API identifier used in `POST /management/commands/{name}`
- `title`: user-facing display label
- `description`: operator help text
- `args`: ordered typed argument definitions

Command names must match `[A-Za-z0-9_.-]+`.

### Supported Argument Types

- `long`
- `double`
- `string`
- `long[]`
- `double[]`
- `string[]`

At execution time, `ManagementCommandService` validates:

- unknown argument names
- missing required arguments
- argument type conversion

The handler receives already-converted Java values:

- `Long`
- `Double`
- `String`
- `long[]`
- `double[]`
- `String[]`

### Return Value

A custom command handler returns `Map<String, Object>`. The returned structure should be JSON-serializable.

Recommended value types:

- `String`
- `Number`
- `Boolean`
- `null`
- `Map<String, Object>`
- `List<?>`
- arrays

### HTTP Shape

Command list response:

```json
{
  "items": [
    {
      "name": "reloadCache",
      "title": "Reload Cache",
      "description": "Reload cache entries for one account.",
      "args": [
        {
          "name": "accountId",
          "title": "Account ID",
          "description": "Account ID",
          "type": "string",
          "required": true
        },
        {
          "name": "partitions",
          "title": "Partitions",
          "description": "Optional partition ids to reload.",
          "type": "long[]",
          "required": false
        }
      ]
    }
  ]
}
```

Command execution request:

```json
{
  "accountId": "sim-001",
  "partitions": [1, 3, 7]
}
```

Command execution response:

```json
{
  "ok": true,
  "accountId": "sim-001",
  "reloadedEntries": 42
}
```

### Operational Notes

- Register commands during application startup, before operators begin using the service.
- Keep handlers fast and deterministic where possible.
- Return explicit fields rather than embedding human-only text.
- For destructive operations, require explicit arguments such as IDs, scopes, or reasons.
- These commands are available both over direct HTTP management and, when applicable, through the gateway-hub management tunnel.
