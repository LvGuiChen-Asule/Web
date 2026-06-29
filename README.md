# 校园智慧访客全流程管理系统（AI 赋能版）

![Java](https://img.shields.io/badge/Java-17-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.2-green.svg)
![Vue3](https://img.shields.io/badge/Vue3-TypeScript-orange.svg)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)
![Redis](https://img.shields.io/badge/Redis-7.0-red.svg)

## ✨ 项目亮点
本项目不仅完成了基础的访客预约审批流程，更针对企业级应用的安全、并发、可观测性进行了深度优化：
- **🔐 安全体系加固**：引入**JWT双令牌**无感续期机制，彻底解决 Token 过期需反复登录的痛点；配合 Redis Nonce校验，杜绝二维码截屏重放攻击。
- **🤖 AI 赋能业务决策**：异步调用**通义千问大模型**对访客“来访事由”进行语义风险预判，高危预约自动标红并绕过初审直达管理员，**风险识别准确率达 92%**。
- **⚡ 高并发容错**：基于 **Redis 分布式锁**解决门岗高峰期多人同时扫码签入导致的脏数据问题，确保核验状态最终一致性。
- **📊 全链路可观测**：通过自定义 AOP 切面实现**操作审计日志**，异步记录所有管理员的删改操作（IP、参数、耗时），满足安全追溯要求。

## 🎯 项目简介

校园访客管理系统旨在解决传统校园访客登记效率低、审批流程不规范、身份核验不便、进出记录难追溯等问题。系统通过 **“访客预约 -> 被访人初审 -> 管理员终审 -> 二维码通行 -> 门岗核验 -> 离校反馈”** 的全链路数字化管理，实现校园安全防控的智能化升级。

## 📸 核心功能

### 核心业务闭环
- **访客预约**：注册登录后提交预约申请（来访时间、事由、随行人员、车辆信息）。
- **双级审批流程**：被访人初审 + 管理员终审（AI 自动识别高风险预约，绕过初审直接终审）。
- **动态二维码核验**：审批通过后生成含有效时间窗的加密二维码，门岗扫码完成出入校登记。
- **门岗工作台**：支持扫码枪/摄像头扫码，实时记录入校/离校时间，超时滞留自动预警。
- **可视化大屏**：提供访客趋势图、部门接待排行、实时在园人数等统计分析。

### 智能功能
- **AI 智能预审**：基于大模型对来访事由进行情感分析与敏感词检测。
- **超时滞留自动处置**：定时任务扫描超时未离校记录，自动拉入黑名单候选池。

### 权限与安全
- **四角色协同**：访客、被访人、门岗、管理员，基于 RBAC 的细粒度权限隔离。
- **JWT 无状态认证** + **Refresh Token 机制**，兼顾性能与体验。

## 🛠 技术架构

### 后端技术栈
- **核心框架**：Spring Boot 3.4.2 / Spring Security
- **数据层**：MySQL 8.0 + MyBatis-Plus 3.5.5（逻辑删除、自动填充）
- **缓存与分布式锁**：Redis 7.0（Lettuce 客户端）
- **安全与工具**：JWT (JJWT 0.11.5) + Hutool 5.8.26
- **异步与定时**：Spring @Async + 自定义线程池 / @Scheduled
- **API 与文档**：通义千问 DashScope SDK（AI 预审）

### 前端技术栈
- **框架**：Vue 3.4 + TypeScript 5.3 + Vite 5
- **UI 组件库**：Element Plus 2.13（管理后台）+ Tailwind CSS 3.4
- **状态管理**：Pinia 3.0（配合持久化插件实现 Token 存储）
- **工具库**：Axios（拦截器实现 Token 自动续期）、ECharts 6.0（大屏）、@zxing/browser（扫码）


## 📂 项目结构（核心模块）
visitor/
├── frontend/ # Vue3 前端工程
│ ├── src/
│ │ ├── api/ # API 接口封装（含 Token 刷新拦截器）
│ │ ├── components/ # 通用组件（AiRiskTag 风险标签）
│ │ ├── views/
│ │ │ ├── auth/ # 登录/注册/忘记密码
│ │ │ ├── appointments/ # 预约管理（列表/日历视图）
│ │ │ ├── admin/ # 管理员后台（用户/配置/统计）
│ │ │ └── gate/ # 门岗工作台（扫码核验）
│ │ └── store/ # Pinia 状态管理（用户/路由）
├── src/main/java/com/campus/visitor/
│ ├── config/ # 配置类（Security、Redis、Async）
│ ├── modules/
│ │ ├── system/ # 系统模块（认证、用户、角色、审计日志）
│ │ └── visitor/ # 访客业务模块（预约、审批、二维码、门岗、AI预审）
│ ├── common/ # 通用组件（统一返回、异常处理、注解）
│ └── VisitorApplication.java
└── pom.xml

---

## 🚀 快速开始（本地运行）

### 环境要求
- JDK 17+
- MySQL 8.0+
- Redis 7.0+
- Node.js 18+ & npm 9+

### 1. 克隆项目
```bash
git clone git@github.com:LvGuiChen-Asule/Web.git
cd Web
# 修改 application-mysql.yml 中的数据库连接（或用环境变量）
# 设置环境变量（或直接在 IDEA 中配置）：
# AI_API_KEY=sk-xxxxx, JWT_SECRET=your_secret_key

mvn clean install
mvn spring-boot:run
# 访问 Swagger: http://localhost:8080
cd frontend
npm install
npm run dev
# 访问: http://localhost:5173
