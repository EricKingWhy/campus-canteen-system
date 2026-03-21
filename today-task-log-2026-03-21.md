# 2026-03-21 任务日志（图片资产全链路修复）

## 1. 故障现象
- 小程序端与管理员端菜品图片出现大面积白板/裂图。
- 典型报错链路：`Failed to load local image resource /static/dish/...` 与历史出现的 `/undefined/static/...`。

## 2. 根因定位
- 前端多个页面对 `dish.image` 的处理不统一，存在直接用相对路径渲染、缺少统一拼接函数的问题。
- 历史数据修复后，后端与数据库已基本统一为相对路径，但前端仍可能出现空值或非标准路径导致渲染失败。

## 3. 今日完成的修复
- 小程序端：
  - 多个页面统一接入图片解析逻辑 `resolveImageUrl(...)`，对 `http`、`/static/dish/`、裸文件名做兜底。
  - 在 `index_v2.vue` 增加 `resolveDishImage(...)`，替换卡片与热销榜图片绑定，防止 `baseUrl/路径异常`导致白板。
- 管理员端：
  - `dish/index.vue` 增加 `resolveDishImage(...)`，统一菜品图片渲染规则。
  - `dish.ts` 修复图片修复接口路径为 `/admin/dish/fix-images`。
- 后端：
  - `DishServiceImpl` 新增并接入图片路径归一化，确保入库与出参统一为 `/static/dish/xxx`。
  - `DbFixController` 中错误外链样例修正为标准本地相对路径。

## 4. 数据与资源核查
- 通过管理员登录接口获取 token 后，拉取菜品分页数据全量核对：
  - 记录总数：`54`
  - 图片字段空值：`0`
  - 图片路径格式异常：`0`
  - 图片文件缺失（数据库记录 vs `server/src/main/resources/static/dish`）：`0`
- HTTP 可达性抽查与全量状态检查：
  - `http://127.0.0.1:8081/static/dish/...` 返回 `200`（全量校验无坏链）。

## 5. 构建验证
- 小程序：`npm run build:mp-weixin` 通过。
- 管理员端：`npm run build` 通过。

## 6. 备注
- 代码中仍有一个非图片业务地址 `ws://localhost:8081/ws/...`（管理员端 websocket），未改动。
- 该项不影响本次图片渲染链路。
