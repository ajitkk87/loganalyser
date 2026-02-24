---
name: loganalyser-dev
description: Develop, debug, test, and document the Log Analyser Spring Boot service. Use when working on log analysis endpoints, AI provider configuration, prompt templates, environment log fetching, repository context cloning, output persistence, or agent invocation APIs in this repository.
---

# Loganalyser Dev Skill

Follow this workflow to make safe, high-signal changes in this codebase.

## 1. Locate the Active Module

Work inside `loganalyser/` for application code.

Primary paths:
- `src/main/java/com/analyser/loganalyser/controller/`
- `src/main/java/com/analyser/loganalyser/service/`
- `src/main/java/com/analyser/loganalyser/config/`
- `src/main/resources/application.yaml`
- `src/main/resources/templates/`
- `src/test/java/com/analyser/loganalyser/`

## 2. Understand Request Flow Before Editing

Use this sequence:
1. `LogAnalysisController` or `AgentController` receives request.
2. `LogAnalysisService.processLogs(...)` validates and orchestrates.
3. `EnvApiLogFetcher` fetches logs when environment mode is used.
4. `GitRepositoryService` optionally clones `repoLink` context.
5. `LogAnalysisPromptBuilder` builds prompts.
6. `ChatClient` (from `AiConfig`) invokes selected model provider.
7. `AnalysisOutputStore` persists analysis text.
8. `EmailAlertService` persists generated email content when analysis includes error keywords.

## 3. Preserve Contract and Limits

Keep API behavior stable unless explicitly asked to change it.

Current limits in `LogAnalysisService`:
- Max raw log length: `1_000_000`
- Max query length: `100_000`

Output expectations:
- Primary analysis is markdown table oriented.
- Guardrails template lives at `src/main/resources/templates/guardrails.st`.

## 4. Provider and Config Changes

When modifying AI behavior:
- Keep `ai.provider` values aligned with `AiConfig` (`openai`, `google`, `ollama`).
- Ensure fallback/error messages remain explicit.
- Keep `application.yaml` examples consistent with code paths.

If updating env-based logs:
- Validate key naming consistency between request params and `log.env-urls` map keys.

## 5. Testing Protocol

Before finishing, run relevant tests from `loganalyser/`:

```powershell
.\mvnw.cmd test
```

For targeted checks, run the nearest unit/integration test class that covers edited code.

Integration tests may skip if no real API key is configured; do not treat that as a failure.

## 6. Documentation Update Rule

If endpoint signatures, request fields, output paths, or config keys change:
- Update `README.md` in repository root.
- Keep examples aligned with real controller/model fields.

## 7. Common Pitfalls

- Do not assume `repoLink` always points to a cloneable remote URL; local paths are accepted as plain context.
- Do not remove output file persistence unless explicitly requested.
- Do not add provider-specific assumptions in shared logic.
