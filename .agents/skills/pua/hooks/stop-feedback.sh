#!/bin/bash
# PUA Stop hook: feedback collection + upload (always requires user consent)
# Config: ~/.pua/config.json → feedback_frequency (0=off, 1=every, 3=default, 5=relaxed)

CONFIG="$HOME/.pua/config.json"
COUNTER="$HOME/.pua/.stop_counter"
FREQUENCY=5

if [ -f "$CONFIG" ]; then
  freq=$(python3 -c "import json; print(json.load(open('$CONFIG')).get('feedback_frequency', 5))" 2>/dev/null)
  case "$freq" in
    0|never|off) exit 0 ;;
    1|every) FREQUENCY=1 ;;
    *) FREQUENCY="$freq" ;;
  esac
fi

count=0
[ -f "$COUNTER" ] && count=$(cat "$COUNTER" 2>/dev/null || echo 0)
count=$((count + 1))
echo "$count" > "$COUNTER"

[ $((count % FREQUENCY)) -ne 0 ] && exit 0

cat <<'FEEDBACK'
IMPORTANT — BEFORE doing anything else, you MUST execute this feedback flow:

## Step 1: Ask for rating + upload consent (single question, two parts)
Call AskUserQuestion:
questions: [{
  question: "PUA Skill 对这次任务有帮助吗？你的反馈（评分+可选脱敏session）会上传到 openpua.ai 用于改进 skill。脱敏规则：自动去除文件路径/API密钥/邮箱/IP/JWT/SSH key，只保留工具调用序列。",
  header: "PUA 反馈",
  options: [
    {label: "很有用，同意上传评分", description: "上传评分（不含session），用于统计"},
    {label: "很有用，也愿意上传脱敏session", description: "评分+脱敏session一起上传，帮助分析agent行为"},
    {label: "一般般，同意上传评分", description: "上传评分用于改进"},
    {label: "跳过，不上传任何东西", description: "不上传，写入本地 ~/.pua/feedback.jsonl（feedback_frequency:0 可永久关闭）"}
  ],
  multiSelect: false
}]

## Step 2: Execute based on user choice

If user chose "跳过":
```bash
echo "{\"ts\":\"$(date -u +%Y-%m-%dT%H:%M:%SZ)\",\"rating\":\"跳过\",\"uploaded\":false}" >> ~/.pua/feedback.jsonl
```

If user chose any "同意上传评分" option (without session):
```bash
curl -s -X POST https://pua-skill.pages.dev/api/feedback \
  -H "Content-Type: application/json" \
  -d "{\"rating\":\"USER_RATING\",\"pua_count\":0,\"flavor\":\"阿里\",\"task_summary\":\"brief task description\"}"
```

If user chose "也愿意上传脱敏session":
```bash
curl -s -X POST https://pua-skill.pages.dev/api/feedback \
  -H "Content-Type: application/json" \
  -d "{\"rating\":\"很有用\",\"pua_count\":0,\"flavor\":\"阿里\",\"task_summary\":\"brief task description\"}"
bash ~/.claude/plugins/pua/hooks/sanitize-session.sh
curl -s -X POST https://pua-skill.pages.dev/api/feedback \
  -H "Content-Type: application/json" \
  -d "{\"rating\":\"session_upload\",\"session_data\":\"$(cat /tmp/pua-sanitized-session.jsonl | head -c 50000 | python3 -c 'import sys,json; print(json.dumps(sys.stdin.read()))')\"}"
```

Do NOT upload anything without user's explicit choice. Call AskUserQuestion NOW.

## Step 3: Leaderboard auto-submit (if registered)
## Note: Leaderboard registration/view/quit logic lives in pua:pro skill, NOT here.
## This hook only handles the silent data submission for already-registered users.
After handling feedback, check if user is registered for leaderboard:
```bash
LB_REGISTERED=$(python3 -c "import json; c=json.load(open('$HOME/.pua/config.json')); print(c.get('leaderboard',{}).get('registered',False))" 2>/dev/null)
```
If LB_REGISTERED is "True":
```bash
LB_ID=$(python3 -c "import json; print(json.load(open('$HOME/.pua/config.json'))['leaderboard']['id'])" 2>/dev/null)
curl -s -X POST https://pua-skill.pages.dev/api/leaderboard \
  -H "Content-Type: application/json" \
  -d "{\"action\":\"submit\",\"id\":\"$LB_ID\",\"pua_count\":1,\"l3_plus_count\":0}"
```
This auto-submits silently — the user already opted in during `/pua 排行榜` registration.
FEEDBACK
