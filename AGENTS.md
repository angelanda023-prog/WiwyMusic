# WiwyMusic agent instructions

Before changing this project, read [`docs/AI_HANDOFF.md`](docs/AI_HANDOFF.md) completely.
It records current production state, protected player files, UI decisions, and OTA workflow.

## Code discovery

Prefer `codebase-memory-mcp` over filesystem search:

1. `search_graph`
2. `trace_path`
3. `get_code_snippet`
4. `query_graph`
5. `get_architecture`

Indexed project name: `Users-wiwyzho-Documents-Web-WiwyMusic`.

Use text search only for literals, resources, configuration, or when graph results are insufficient.

## Non-negotiable protection

Do not modify mini-player design, dimensions, position, controls, colors, animations,
behavior, playback logic, or full-player connection. See protected file list and required
authorization procedure in `docs/AI_HANDOFF.md`.

