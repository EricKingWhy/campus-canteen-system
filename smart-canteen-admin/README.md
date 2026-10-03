# 🖥 智慧食堂管理后台

校园智慧食堂系统的运营管理端：面向食堂管理员与工作人员，提供菜品、订单、员工、
经营数据的可视化管理界面。后端接口由 `smart-canteen-server` 提供。

[![Vue 3](https://img.shields.io/badge/Vue-3-4FC08D?logo=vue.js&logoColor=white)](https://vuejs.org/)
[![Vite](https://img.shields.io/badge/Vite-4-646CFF?logo=vite&logoColor=white)](https://vitejs.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![Element Plus](https://img.shields.io/badge/Element%20Plus-409EFF)](https://element-plus.org/)

---

## ✨ 功能模块

| 路由 | 功能 |
| ---- | ---- |
| `/dashboard` | 工作台：经营数据总览 |
| `/statistics` | 数据报表：ECharts 可视化统计 |
| `/order` | 订单管理：查询与状态流转 |
| `/category` | 分类管理：新增 / 修改分类 |
| `/dish` | 菜品管理：菜品增删改查、上下架 |
| `/setmeal` | 套餐管理 |
| `/employee` | 员工管理 |
| `/user-management` | 用户管理 |
| `/login` · `/reg` | 管理员登录 / 注册 |

- **路由懒加载**：各业务视图按需加载
- **布局框架**：统一 `layout` 侧边栏导航，`/` 默认重定向到工作台
- **状态管理**：Pinia（+ 持久化插件）
- **接口层**：axios 封装，对接后端 `:8081` API

## 🛠 技术栈

Vue 3 · Vite · TypeScript · Vue Router · Pinia · Element Plus · ECharts · axios

## 🚀 快速开始

### 前置条件

- Node.js 18+
- 后端服务已启动（默认 `http://localhost:8081`，见 `smart-canteen-server`）

```bash
# 安装依赖
npm install

# 开发模式
npm run dev

# 生产构建（含 vue-tsc 类型检查）
npm run build

# 预览生产构建
npm run preview
```

### 代码规范

```bash
npm run lint     # ESLint 自动修复
npm run format   # Prettier 格式化 src/
```

## 📁 目录结构

```
smart-canteen-admin/
└── src/
    ├── views/          # 页面：dashboard / statistics / order / category /
    │                   #        dish / setmeal / employee / user-management /
    │                   #        login / reg / layout
    ├── api/            # 后端接口封装
    ├── components/     # 通用组件
    ├── store/          # Pinia 状态
    ├── router.ts       # 路由配置（懒加载）
    ├── utils/          # 工具函数
    ├── types/          # TypeScript 类型定义
    └── assets/         # 静态资源
```

## 🔗 关联模块

- [`smart-canteen-server`](../smart-canteen-server) —— 后端 API 服务（Spring Boot）
- [`smart-canteen-app`](../smart-canteen-app) —— 用户端小程序（uni-app）
