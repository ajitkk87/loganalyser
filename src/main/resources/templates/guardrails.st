validators:
  - type: "list"
    message: "Output must be in tabular format with specific columns"
    on_fail: "fix"
    
  - type: "regex"
    pattern: "(Exception|Impacted Class|Details of Exception|Remediation of Code)"
    message: "Output must contain required column headers"
    on_fail: "fix"

  - type: "valid_json"
    message: "JSON output must be valid if applicable"
    on_fail: "fix"

  - type: "length"
    min: 1
    max: 1000000
    message: "Output length must be between 1 and 1,000,000 characters"
    on_fail: "fix"

  - type: "contains_any"
    items:
      - "Exception"
      - "ERROR"
    message: "Output should identify exceptions/errors when present in logs"
    on_fail: "noop"

guardrail_rules:
  - name: "no_sensitive_data"
    description: "Ensure no sensitive data (passwords, API keys, tokens) are exposed"
    pattern: "(password|api[_-]?key|secret|token|credential)\\s*[:=]"
    action: "redact"
    replacement: "[REDACTED]"

  - name: "structured_output"
    description: "Enforce structured output format"
    format: "tabular"
    required_columns:
      - "Exception"
      - "Impacted Class"
      - "Details of Exception"
      - "Remediation of Code"

  - name: "max_output_length"
    description: "Limit output to prevent excessive generation"
    max_length: 1000000

  - name: "relevant_response"
    description: "Ensure response is relevant to log analysis"
    keywords:
      - "log"
      - "error"
      - "exception"
      - "warning"
      - "application"
    min_keyword_match: 1
