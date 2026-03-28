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
