# json schema api service

A json schema service useful for linting AUD files with description of fields, and basic fields and syntax checks.

## Running it

- `just start-json-schema-dev` — Quarkus dev mode

Once started, the service is on <http://localhost:8080> and exposes:

- V1 YAML JSON Schema: <http://localhost:8080/v1/yaml-schema>

This schema endpoint can be used with your editor to lint AUDs.

## How to use it

Depending on the editor, you might need to install
[yaml language server](https://github.com/redhat-developer/yaml-language-server), and point it to the schema endpoint.

### VSCode [TESTED OK]

Install the [yaml plugin](https://marketplace.visualstudio.com/items?itemName=redhat.vscode-yaml)

Then update this file: `.vscode/settings.json`:

```json
{
  "yaml.schemas": {
    "http://localhost:8080/v1/yaml-schema": [
      "*.yaml",
      "*.yml"
    ]
  }
}
```

### Helix Editor [TESTED OK]

In your AUD scope, add this file: `.helix/languages.toml`

```toml
[[language]]
name = "yaml"
language-servers = ["yaml-language-server"]

[language-server.yaml-language-server.config.yaml.schemas]
"http://localhost:8080/v1/yaml-schema" = ["**/*.yaml", "**/*.yml"]
```

### Intellij [BROKEN]

Intellij supports json schemas for yaml, but has bugs making it broken. Avoid using it.
