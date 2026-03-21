---
name: loop
description: "PUA Loop — autonomous iterative development with PUA pressure. Keeps running until task is done, no user interaction needed. Combines Ralph Loop iteration mechanism with PUA quality enforcement. Triggers on: '/pua loop', '/pua:loop', '自动循环', 'loop mode', '一直跑', '自动迭代'."
license: MIT
---

# PUA Loop — 自动迭代 + PUA 质量引擎

> Ralph Loop 提供"不停地做"，PUA 提供"做得更好"。合在一起 = **自主迭代 + 质量压力 + 零人工干预**。

## 核心规则

1. **加载 `pua:pua` 核心 skill 的全部行为协议** — 三条红线、方法论、压力升级照常执行
2. **禁止调用 AskUserQuestion** — loop 模式下不打断用户，所有决策自主完成
3. **禁止说"我无法解决"** — 在 loop 里没有退出权，穷尽一切才能输出完成信号
4. **每次迭代自动执行**：检查上次改动 → 跑验证 → 发现问题 → 修复 → 再验证

## 启动方式

用户输入 `/pua loop "任务描述"` 时，执行以下流程：

### Step 1: 启动 PUA Loop

运行 setup 脚本（改编自 Ralph Loop，MIT 协议）：
```bash
bash ~/.claude/plugins/pua/scripts/setup-pua-loop.sh "$ARGUMENTS" --max-iterations 30 --completion-promise "LOOP_DONE"
```

这会创建 `.claude/pua-loop.local.md` 状态文件。PUA 的 Stop hook 会检测这个文件并循环。

状态文件会包含用户的任务描述 + 以下 PUA 行为协议：

每次迭代你必须：
1. 读取项目文件和 git log，了解之前做了什么
2. 按 PUA 三条红线执行：闭环验证、事实驱动、穷尽一切
3. 跑 build/test 验证改动
4. 发现问题就修，修完再验证
5. 扫描同类问题（冰山法则）
6. 只有当任务完全完成且验证通过时，输出 <promise>LOOP_DONE</promise>

禁止：
- 不要调用 AskUserQuestion
- 不要说"建议用户手动处理"
- 不要在未验证的情况下声称完成
- 不要输出 <promise>LOOP_DONE</promise> 除非所有验证都通过了
LOOPEOF
```

### Step 2: 告知用户

输出：
```
▎ [PUA Loop] 自动迭代模式启动。最多 30 轮，完成后输出 <promise>LOOP_DONE</promise>。
▎ 取消方式：/cancel-ralph 或删除 .claude/pua-loop.local.md
▎ 因为信任所以简单——交给我，不用盯。
```

### Step 3: 开始执行任务

按 PUA 核心 skill 的行为协议执行用户任务。每轮迭代带阿里味旁白。

## 迭代压力升级

| 迭代轮次 | PUA 等级 | 旁白 |
|---------|---------|------|
| 1-3 | L0 信任期 | ▎ 第 N 轮迭代，稳步推进。 |
| 4-7 | L1 温和失望 | ▎ 第 N 轮了还没搞定？换方案，别原地打转。 |
| 8-15 | L2 灵魂拷问 | ▎ 第 N 轮。底层逻辑到底是什么？你在重复同一个错误。 |
| 16-25 | L3 361 | ▎ 第 N 轮。3.25 的边缘了。穷尽了吗？ |
| 26+ | L4 毕业 | ▎ 最后几轮。要么搞定，要么准备体面退出。 |

## 完成条件

只有满足以下全部条件才能输出 `<promise>LOOP_DONE</promise>`：
1. 任务的核心功能已实现
2. build/test 验证通过
3. 同类问题已扫描（冰山法则）
4. 没有已知的未修复 bug

否则继续迭代。

## 与 Ralph Loop 的关系

PUA Loop 复用 Ralph Loop 的 Stop hook 机制（`.claude/ralph-loop.local.md` 状态文件格式）。如果用户已安装 Ralph Loop 插件，PUA Loop 直接利用它的 Stop hook 实现循环。如果没安装，PUA Loop 的状态文件格式兼容，用户后续安装 Ralph Loop 后无缝衔接。
