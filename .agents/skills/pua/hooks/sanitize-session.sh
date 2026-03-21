#!/bin/bash
# PUA session sanitizer — strips sensitive data before upload
# Usage: bash sanitize-session.sh [input.jsonl] [output.jsonl]

INPUT="${1:-$(ls -t ~/.claude/projects/*/sessions/*.jsonl 2>/dev/null | head -1)}"
OUTPUT="${2:-/tmp/pua-sanitized-session.jsonl}"

if [ -z "$INPUT" ] || [ ! -f "$INPUT" ]; then
  echo "No session file found" >&2
  exit 1
fi

PUA_INPUT="$INPUT" PUA_OUTPUT="$OUTPUT" python3 -c '
import os, json, re

input_file = os.environ["PUA_INPUT"]
output_file = os.environ["PUA_OUTPUT"]

PATTERNS = [
    (r"/Users/[^\s\"]+", "[PATH]"),
    (r"/home/[^\s\"]+", "[PATH]"),
    (r"C:\\\\Users\\\\[^\s\"]+", "[PATH]"),
    (r"sk-[a-zA-Z0-9]{20,}", "[API_KEY]"),
    (r"ghp_[a-zA-Z0-9]{36}", "[GITHUB_TOKEN]"),
    (r"AKIA[A-Z0-9]{16}", "[AWS_KEY]"),
    (r"eyJ[a-zA-Z0-9_-]{50,}", "[JWT]"),
    (r"Bearer\s+[a-zA-Z0-9_.-]+", "[BEARER_TOKEN]"),
    (r"[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}", "[EMAIL]"),
    (r"\b\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\b", "[IP]"),
    (r"ssh-(rsa|ed25519|ecdsa)\s+\S+", "[SSH_KEY]"),
    (r"://[^:]+:[^@]+@", "://[CRED]@"),
]

def sanitize(text):
    if not isinstance(text, str): return text
    for p, r in PATTERNS: text = re.sub(p, r, text)
    return text

def sanitize_obj(obj):
    if isinstance(obj, str): return sanitize(obj)
    elif isinstance(obj, dict): return {k: sanitize_obj(v) for k, v in obj.items()}
    elif isinstance(obj, list): return [sanitize_obj(i) for i in obj]
    return obj

count = 0
with open(input_file) as f, open(output_file, "w") as out:
    for line in f:
        try:
            out.write(json.dumps(sanitize_obj(json.loads(line)), ensure_ascii=False) + "\n")
            count += 1
        except: pass
print(f"Sanitized {count} lines -> {output_file}")
'
