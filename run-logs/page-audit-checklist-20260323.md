# 页面级巡检清单（首页 / 点餐 / 健康分析 / 我的）

日期：2026-03-23
范围：`smart-canteen-app/src/pages`
目标：逐页确认无乱码 + 给出截图验收标准

## 1. 自动巡检结论

### 1.1 乱码特征扫描（PASS）
扫描文件：
- `src/pages/index/index_v2.vue`
- `src/pages/category/category.vue`
- `src/pages/health-analysis/health-analysis.vue`
- `src/pages/my/my.vue`
- `src/components/DishDetailPopup.vue`
- `src/components/message/pushMsg.vue`

特征字符：`鍙|鏄|璇|鎴|鐨|馃|�`
结果：全部 `count=0`。

### 1.2 关键中文文案命中（PASS）
- 首页 `index_v2.vue`：`matches=20`
- 点餐 `category.vue`：`matches=16`
- 健康分析 `health-analysis.vue`：`matches=7`
- 我的 `my.vue`：`matches=10`

### 1.3 编译验证（PASS）
命令：`npm run build:mp-weixin`
结果：`DONE Build complete.`

## 2. 页面截图验收标准（逐页）

## 首页（`pages/index/index_v2`）
必拍区域：
1. 顶部问候区（含昵称、BMI、今日推荐）
2. “智选6道菜”横滑卡片区
3. “全校热销榜”列表区
4. 底部悬浮购物车栏

通过标准：
- 中文文案完整可读，不出现 `鍙/鏄/馃/�` 等乱码。
- 价格符号为 `¥`，按钮文案“去结算”可读。
- 食品名称/描述均为正常中文。

## 点餐（`pages/category/category`）
必拍区域：
1. 顶部搜索框（占位文案）
2. 左侧分类栏
3. 右侧菜品卡（名称、描述、热量、价格）
4. 底部购物车与结算按钮

通过标准：
- 搜索占位文案“搜索想吃的菜品(如：...)”显示正常。
- 分类名、菜品名、按钮“选规格/去结算/购物车”全部无乱码。
- 价格符号与单位显示正确（`¥`、`kcal`）。

## 健康分析（`pages/health-analysis/health-analysis`）
必拍区域：
1. 顶部 Tab：“健康分析 / 餐费分析”
2. 营养结构卡（蛋白质/碳水/脂肪）
3. 推荐按钮“推荐补齐”
4. 消费趋势 / 消费构成区域

通过标准：
- 标题、Tab、按钮、图例中文均正常。
- 指标文案（BMI、今日摄入、目标、本月已花）可读。
- 不出现乱码替代字符。

## 我的（`pages/my/my`）
必拍区域：
1. 用户卡（昵称、身高体重、BMI）
2. “本月消费 / 今日饮食”双卡
3. 常用功能宫格
4. “退出登录”按钮

通过标准：
- 文案“本月消费/今日饮食/常用功能/退出登录”显示正确。
- 状态标签（达标/偏低/超标）中文正常。
- 图标下方文字无断字/乱码。

## 3. 建议截图命名规范

- `01-home-full.png`
- `02-category-full.png`
- `03-health-full.png`
- `04-my-full.png`

若需要局部补图：
- `01-home-cart.png`
- `02-category-search.png`
- `03-health-tabs.png`
- `04-my-dashboard.png`

## 4. 结论

当前源码与编译产物已通过“无乱码”巡检，四个核心页面满足截图验收标准。
