# 智能食堂项目 MCP 与 Skills 清单（全局 + 项目级）

更新时间：2026-03-20  
项目路径：`C:\Users\王浩宇\Desktop\毕业设计工具\校园食堂管理系统\smart-canteen-main\smart-canteen-main`

## 1. 全局 MCP（Codex）

全局配置文件路径：`C:\Users\王浩宇\.codex\config.toml`

### 1.1 已启用（常驻/默认可用）

1. `filesystem`
- 功能：本地文件系统读写、目录遍历、文件管理。
- 主要命令：`@modelcontextprotocol/server-filesystem`（npx）。
- 路径：来自 [config.toml](/C:/Users/王浩宇/.codex/config.toml) 的 `[mcp_servers.filesystem]`。

2. `everything-search`
- 功能：Windows 全盘快速搜索（基于 Everything 能力）。
- 主要命令：`mcp-everything-search.exe`（Python 可执行）。
- 路径：来自 [config.toml](/C:/Users/王浩宇/.codex/config.toml) 的 `[mcp_servers.everything-search]`。

3. `fetch`
- 功能：网页内容抓取、可读化提取、JSON/文本获取。
- 主要命令：`mcp-fetch-server`（npx）。
- 路径：来自 [config.toml](/C:/Users/王浩宇/.codex/config.toml) 的 `[mcp_servers.fetch]`。

4. `memory`
- 功能：长期记忆（知识图谱/记忆文件持久化）。
- 主要命令：`@modelcontextprotocol/server-memory`（npx）。
- 记忆文件：`C:/Users/王浩宇/.codex/memories/global-memory.jsonl`
- 路径：来自 [config.toml](/C:/Users/王浩宇/.codex/config.toml) 的 `[mcp_servers.memory]`。

5. `sequential-thinking`
- 功能：分步推理/思维链流程，适合复杂问题拆解。
- 主要命令：`@modelcontextprotocol/server-sequential-thinking`（npx）。
- 配置：`DISABLE_THOUGHT_LOGGING = "true"`（减少日志/噪音）。
- 路径：来自 [config.toml](/C:/Users/王浩宇/.codex/config.toml) 的 `[mcp_servers.sequential-thinking]`。

6. `exa`
- 功能：Exa 网络搜索与高质量检索。
- 主要命令：`exa-mcp-server`（npx）。
- 路径：来自 [config.toml](/C:/Users/王浩宇/.codex/config.toml) 的 `[mcp_servers.exa]`。

### 1.2 已配置但默认禁用（按需/休眠）

1. `github`（`enabled = false`）
- 功能：GitHub 仓库、Issue、PR、文件操作。
- 主要命令：`@modelcontextprotocol/server-github`（npx）。
- 路径：来自 [config.toml](/C:/Users/王浩宇/.codex/config.toml) 的 `[mcp_servers.github]`。

2. `context7`（`enabled = false`）
- 功能：重型上下文分析/文档上下文能力（封印状态）。
- 主要命令：`context7`（npx）。
- 路径：来自 [config.toml](/C:/Users/王浩宇/.codex/config.toml) 的 `[mcp_servers.context7]`。

3. `playwright`（`enabled = false`）
- 功能：浏览器自动化测试与抓取（封印状态）。
- 主要命令：`@executeautomation/playwright-mcp-server`（npx）。
- 路径：来自 [config.toml](/C:/Users/王浩宇/.codex/config.toml) 的 `[mcp_servers.playwright]`。

### 1.3 全局额外项（历史残留）

1. `pencil`
- 功能：连接 Antigravity/Pencil 应用。
- 状态：配置存在，是否可用取决于 Antigravity 端连接。
- 路径：来自 [config.toml](/C:/Users/王浩宇/.codex/config.toml) 的 `[mcp_servers.pencil]`。

## 2. 项目级 MCP

项目配置文件路径：`C:\Users\王浩宇\Desktop\毕业设计工具\校园食堂管理系统\smart-canteen-main\smart-canteen-main\mcp.json`

1. `mysql`
- 功能：连接 MySQL，执行查询/更新，支撑业务数据分析。
- 命令：`@f4ww4z/mcp-mysql-server`（npx）。
- 路径：来自 [mcp.json](/C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/mcp.json) 的 `mcpServers.mysql`。

2. `redis`
- 功能：缓存访问、键值读取（菜单缓存与并发数据）。
- 命令：`redis-mcp-server.exe`（Python 可执行）。
- 路径：来自 [mcp.json](/C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/mcp.json) 的 `mcpServers.redis`。

3. `openapi`
- 功能：读取并映射 OpenAPI 文档，支持接口参数核对与动态工具生成。
- 命令：`openapi_mcp_server.exe`。
- 文档源：`http://localhost:8081/v3/api-docs`
- 路径：来自 [mcp.json](/C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/mcp.json) 的 `mcpServers.openapi`。

4. `echarts`
- 功能：ECharts 图表配置生成与可视化协作（成本分析报表）。
- 命令：`mcp-echarts`（npx）。
- 路径：来自 [mcp.json](/C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/mcp.json) 的 `mcpServers.echarts`。

5. `sql-analyzer`
- 功能：SQL 语法检查、方言转换、表/列引用分析（静态分析向）。
- 命令：`mcp-server-sql-analyzer.exe`。
- 路径：来自 [mcp.json](/C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/mcp.json) 的 `mcpServers.sql-analyzer`。

6. `jetbrains`
- 功能：对接 JetBrains IDE（IntelliJ）进行代码级联动操作。
- 命令：`@jetbrains/mcp-proxy`（npx）。
- 关键配置：`HOST=127.0.0.1`、`IDE_PORT=63342`。
- 路径：来自 [mcp.json](/C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/mcp.json) 的 `mcpServers.jetbrains`。

7. `chrome-devtools`
- 功能：前端调试、网络抓包、控制台分析、页面自动化检查。
- 命令：`chrome-devtools-mcp@latest`（npx）。
- 路径：来自 [mcp.json](/C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/mcp.json) 的 `mcpServers.chrome-devtools`。

## 3. 全局 Skills（Codex 可调用）

全局 Skills 根目录：`C:\Users\王浩宇\.codex\skills\.system`

1. `openai-docs`
- 功能：OpenAI 官方文档检索与构建指导。
- 路径：[SKILL.md](/C:/Users/王浩宇/.codex/skills/.system/openai-docs/SKILL.md)

2. `skill-creator`
- 功能：创建/改造 Skill 的方法与模板流程。
- 路径：[SKILL.md](/C:/Users/王浩宇/.codex/skills/.system/skill-creator/SKILL.md)

3. `skill-installer`
- 功能：安装与管理 Skill（含 curated 和 repo 安装）。
- 路径：[SKILL.md](/C:/Users/王浩宇/.codex/skills/.system/skill-installer/SKILL.md)

## 4. 项目级 Skills（当前项目可调用）

项目 Skills 根目录：`C:\Users\王浩宇\Desktop\毕业设计工具\校园食堂管理系统\smart-canteen-main\smart-canteen-main\.agents\skills`

1. `api-design`
- 功能：REST API 设计规范（命名、状态码、分页、错误模型等）。
- 路径：[SKILL.md](/C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/.agents/skills/api-design/SKILL.md)

2. `article-writing`
- 功能：长文与说明文写作、结构化内容产出。
- 路径：[SKILL.md](/C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/.agents/skills/article-writing/SKILL.md)

3. `autonomous-loops`
- 功能：自治执行链路/循环式代理工作流设计。
- 路径：[SKILL.md](/C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/.agents/skills/autonomous-loops/SKILL.md)

4. `backend-patterns`
- 功能：Node/Express/Next 后端架构与性能实践。
- 路径：[SKILL.md](/C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/.agents/skills/backend-patterns/SKILL.md)

5. `blueprint`
- 功能：复杂任务路线图分解与阶段化执行。
- 路径：[SKILL.md](/C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/.agents/skills/blueprint/SKILL.md)

6. `coding-standards`
- 功能：通用代码规范（TS/JS/React/Node）。
- 路径：[SKILL.md](/C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/.agents/skills/coding-standards/SKILL.md)

7. `database-migrations`
- 功能：MySQL 迁移、回滚、零停机策略。
- 路径：[SKILL.md](/C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/.agents/skills/database-migrations/SKILL.md)

8. `deployment-patterns`
- 功能：部署流程、CI/CD、容器化、回滚。
- 路径：[SKILL.md](/C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/.agents/skills/deployment-patterns/SKILL.md)

9. `frontend-patterns`
- 功能：Vue3 + Uniapp + Pinia 前端实践。
- 路径：[SKILL.md](/C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/.agents/skills/frontend-patterns/SKILL.md)

10. `java-coding-standards`
- 功能：Spring Boot Java 编码规范与工程组织。
- 路径：[SKILL.md](/C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/.agents/skills/java-coding-standards/SKILL.md)

11. `pua-debugging`
- 功能：系统化排障，避免低效试错与误判。
- 路径：[SKILL.md](/C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/.agents/skills/pua-debugging/SKILL.md)

12. `search-first`
- 功能：先调研后开发，优先复用成熟方案。
- 路径：[SKILL.md](/C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/.agents/skills/search-first/SKILL.md)

13. `skill-stocktake`
- 功能：Skills 质量盘点与审计。
- 路径：[SKILL.md](/C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/.agents/skills/skill-stocktake/SKILL.md)

14. `springboot-patterns`
- 功能：Spring Boot 架构模式（分层、缓存、异步、日志）。
- 路径：[SKILL.md](/C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/.agents/skills/springboot-patterns/SKILL.md)

15. `springboot-security`
- 功能：Spring Security 安全实践（鉴权、授权、限流、依赖安全）。
- 路径：[SKILL.md](/C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/.agents/skills/springboot-security/SKILL.md)

16. `strategic-compact`
- 功能：阶段化上下文压缩，减少信息丢失。
- 路径：[SKILL.md](/C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/.agents/skills/strategic-compact/SKILL.md)

17. `verification-loop`
- 功能：验证闭环（检查、测试、结果确认）。
- 路径：[SKILL.md](/C:/Users/王浩宇/Desktop/毕业设计工具/校园食堂管理系统/smart-canteen-main/smart-canteen-main/.agents/skills/verification-loop/SKILL.md)

## 5. 备注

1. `mcp.json`（项目级）与 `C:\Users\王浩宇\.codex\config.toml`（全局）同时存在时，会分别服务于项目与全局场景。  
2. 全局中 `github/context7/playwright` 当前处于禁用状态（`enabled = false`），不会默认常驻。  
3. VSCode 项目内旧 MCP 文件 `.vscode/mcp.json` 与 `.vscode/mcp.full.bak.json` 已删除，减少重复拉起风险。
