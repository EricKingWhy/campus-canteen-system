# 校园食堂管理系统 - 项目日报
**日期：** 2026年3月8日
**开发核心关注点：** C端数据强曝光、前后端字段大一统、数据库结构升级与异常容错增强

## 今日完成要点总览 (Highlights & Enhancements)

今日的工作核心集中在**贯穿全链路（DB -> Mapper -> Spring Boot Backend -> Vue Admin -> UniApp User端）的菜品关键数据一致性同步与强制安全展示**。我们对数据库结构、底层接口穿透逻辑、容灾能力以及客户端界面的渲染层都做出了核弹级的强力调整。

### 1. 数据库升级与底层结构奠基
- **自动 SQL 脚本生成**: 在项目中自动生成并配置了 `V2__Add_Nutrition_And_Allergen_Data.sql` 结构扩展与数据更新脚本。
- **全表关键字段扩展**: 为 MySQL 中的 `dish` 表增加了 `main_ingredients`（主要成分）和 `allergen_tags`（忌口/过敏源）字段的支撑。
- **历史幽灵列肃清**: 针对历史上存在的 `pic` 和 `image` 以及 `detail` 和 `description` 列混用的问题进行了铁腕重构！全核心实体（Entity、DTO、VO）强制统一至：
  - 图片字段：**统一使用 `image`**。
  - 详情描述字段：**统一使用 `description`**。

### 2. DTO 高容灾与接口错误拦截
- **DTO 别名映射与容错**: 在后端 `DishDTO.java` 中引入 `@JsonAlias({"pic", "image"})` 与 `@JsonAlias({"detail", "description"})` 双端别名容灾，外加 `@JsonIgnoreProperties(ignoreUnknown = true)` 防护装甲，有效粉碎了任何因前端遗留拼写参数传入造成的 Jackson `UnrecognizedPropertyException` (500错误) 崩溃。

### 3. B端管理系统 (Vue Admin) UI & 逻辑纠偏
- **页面代码错误紧急修复**: 排查修复了管理端 `add.vue` 的 `Invalid end tag` 红屏编译致命崩溃，并在修复时稳健复原了丢失的基于 Element Plus 的图片上传核心组件。
- **前后端表单深度绑定与严苛校验**:
  - `add.vue` 中强行将详情字段的绑定由陈旧的 `detail` 变更为 `description`，以及 `pic` 修改为 `image`。
  - 增补了“主要成分”的**强防呆必填验证限制**。
  - 补全了“忌口/过敏原”的优雅空值兜底：用户若未填写，系统自动截获并填充为“无”。

### 4. C端用户小程序 (UniApp) 核弹级视效渗透注入
这是今日最关键的战役，为保障用户的饮食安全与商品信息的全景暴露：
- **穿透 API 与日志核查**: 核查了 MyBatis `DishMapper` 并确保 `lambdaQuery()` 和 `selectPage()` 完全传递扩展字段；在 Vue 钩子里埋设了“【核弹级审查】C端收到的菜品完整数据：”级的终端 console 高亮输出节点。
- **独立页暴力注入模板**: 在 `detail.vue` (单独的菜品详情页) 直接注入高亮红绿醒目背景提示框展示成分和过敏原，并成功显示“⚠️ 忌口提示”和“✅ 放心食用”条件级UI。
- **弹窗组件 (DishDetailPopup) 精准注射**: 在定位到购物车与某些分类点击弹窗是独立渲染在 `DishDetailPopup.vue` 组件中时，我们将全量 UI 同样切入到了该组件的 description 下，保证跨页呈现一致！
- **首页“智选6道菜”组件补齐**: 修复了 `index_v2.vue` 中的降维打击问题。此前 `.map` 操作使得后端通过 AI 健康引擎传来的 `allergenTags`、`mainIngredients` 字段丢失；利用 ES6 `...dish` 操作符修复内存拷贝，彻底打通了首页智选卡片弹出的完美组件数据流通链路。

## 明日计划
- 部署最新的前后端编译包进行真机/集成测试。
- 继续关注系统其他模块关于用户营养健康关联数据的推荐逻辑验证。
- 视体验情况进行细节微调。
