# Log Analyser

AI-powered log analysis service built with Spring Boot and Spring AI.

The service accepts either:
- raw logs in request body, or
- environment-based log lookup parameters,

and returns a concise markdown table with detected exceptions, impacted classes, and remediation guidance.

## Project Layout

- `loganalyser/`: main Spring Boot application (Maven module)
- `target/`: build artifacts plus generated analysis/email output and cloned repos

## Features

- Multi-provider AI model selection via `ai.provider` (`openai`, `google`, `ollama`)
- Raw log analysis endpoint
- Environment-source analysis endpoint
- Agent metadata/invocation endpoints (`/api/agent/*`)
- Optional repository context cloning (`repoLink`)
- Guardrails + email template prompt loading from classpath templates
- File output persistence for analysis and generated email alerts

## Tech Stack

- Java 25
- Spring Boot 4.0.2
- Spring AI 2.0.0-M2
- Maven Wrapper (`mvnw`, `mvnw.cmd`)

## Prerequisites

- JDK 25 installed and on `PATH`
- Git installed (required if using `repoLink` with remote git URLs)
- Provider credentials depending on `ai.provider`

## Configuration

Primary config file: `loganalyser/src/main/resources/application.yaml`

### Required properties

- `ai.provider`: one of `openai`, `google`, `ollama`
- For OpenAI-compatible usage:
  - `OPENAI_API_KEY` (or `spring.ai.openai.api-key`)
- For Google GenAI usage:
  - `GOOGLE_API_KEY`
- For Ollama usage:
  - reachable Ollama endpoint (default `http://localhost:11434`)

### Environment URLs

`log.env-urls` maps environment keys to source URLs used by environment analysis:
- `dev`
- `test`
- `acc`
- `prd`

## Run

From repository root:

```powershell
cd loganalyser
.\mvnw.cmd spring-boot:run
```

Or with tests:

```powershell
cd loganalyser
.\mvnw.cmd clean test
```

## API Reference

Base URL: `http://localhost:8080`

### 1. Analyze logs from environment

`POST /api/logs/search-and-analyze-env`

Query params:
- `env` (default: `TST`)
- `query` (default: `Find critical errors`)
- `repoLink` (optional)
- `logLevel` (optional)
- `days` (optional)
- `applicationName` (optional)

Example:

```bash
curl -X POST "http://localhost:8080/api/logs/search-and-analyze-env?env=prd&query=Find%20critical%20errors&logLevel=ERROR&days=3&applicationName=user-service"
```

### 2. Analyze raw log content

`POST /api/logs/search-and-analyze-raw`

Body: `text/plain` raw logs  
Query params: same as above except `env`

Example:

```bash
curl -X POST "http://localhost:8080/api/logs/search-and-analyze-raw?query=Find%20critical%20errors&logLevel=ERROR&repoLink=https://github.com/example/repo" ^
  -H "Content-Type: text/plain" ^
  --data "2026-02-24 12:00:00 ERROR com.example.UserService - NullPointerException while fetching user"
```

### 3. Agent endpoints

- `GET /api/agent/card`
- `GET /api/agent/.well-known/agent-card`
- `POST /api/agent/analyze`

`POST /api/agent/analyze` JSON body:

```json
{
  "logs": "2026-02-24 12:00:00 ERROR com.example.UserService - NullPointerException",
  "query": "Find root cause and remediation",
  "repoLink": "https://github.com/example/repo",
  "logLevel": "ERROR",
  "days": 2,
  "applicationName": "user-service",
  "environment": "prd"
}
```

Response:

```json
{
  "analysis": "| Exception | Impacted Class | Details of Exception | Remediation of Code |\n|---|---|---|---|\n..."
}
```

## Output Files

Generated automatically under repository root:

- Analysis output: `target/log_analysis_output/analysis_<timestamp>.txt`
- Email alert output: `target/email/email/email_<timestamp>.txt`

## Validation Rules

- Max raw log length: `1,000,000` characters
- Max query length: `100,000` characters
- Empty/blank logs are rejected

## Key Classes

- `LogAnalysisController`: REST endpoints for raw/env analysis
- `AgentController`: agent card + invoke endpoint
- `LogAnalysisService`: orchestration and AI calls
- `AiConfig`: provider-based `ChatModel` selection
- `EnvApiLogFetcher`: environment log retrieval stub
- `GitRepositoryService`: optional git clone for repository context
- `PromptTemplateService`: loads `guardrails.st` and `email-alert.st`

## Tests

- Unit tests: service/controller behavior with mocks
- Integration tests: real model invocation (skips unless real API key exists)

Run all:

```powershell
cd loganalyser
.\mvnw.cmd test
```

## Notes

- Keep secrets out of source-controlled `.env` files.
- `repoLink` clones into `target/cloned-repos` when the value looks like a git URL.
