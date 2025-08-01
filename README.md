# 加密货币行情App 开发文档（基础框架及开发流程）

## 1. 项目总览

- **目标：**
  - 实时展示加密货币行情（价格、K线、涨幅等）。
  - 支持自定义关注币种列表。
  - 提供桌面小组件，展示行情并可定时更新。

## 2. 技术栈

- **语言**：Kotlin（推荐）、Java
- **开发工具**：Android Studio
- **网络库**：Retrofit + OkHttp
- **UI 框架**：Jetpack Compose 或 XML（推荐 Compose）
- **数据存储**：Room / DataStore
- **异步流**：Coroutine + Flow
- **K线图**：MPAndroidChart 或 Vico
- **Widget开发**：AppWidgetProvider / Glance

---

## 3. 模块划分与优先级

优先级从高到低建议分阶段迭代实现：

### 3.1 基础功能模块

| 优先级 | 模块             | 说明                                   |
| ------ | ---------------- | -------------------------------------- |
| P0     | 行情卡片展示模块   | 展示币种、价格、涨跌幅、小K线（如你的图片） |
| P0     | 网络数据服务模块   | 对接API，拉取币种价格、K线、涨跌数据       |
| P1     | 关注列表管理模块   | 添加/删除自选币种，本地缓存，重启可恢复     |
| P1     | K线图详情页模块    | 展示专业K线，支持多时间级别切换           |
| P2     | 桌面小组件模块     | 支持自定义显示关注币行情，定时自动刷新     |

### 3.2 后续扩展模块

| 优先级 | 模块             | 说明                                   |
| ------ | ---------------- | -------------------------------------- |
| P3     | 高级通知/提醒服务模块 | 如价格预警通知、上涨提醒等                  |
| P3     | 深度互动模块       | 社区、讨论、自动分析                      |
| P3     | 可视化自定义模块    | K线指标切换、主题切换、界面自定义           |

---

## 4. 模块化开发流程

按优先级推荐如下开发步骤，每步建议先“接口开发与测试”，再进行“UI实现与集成”，最后“联调和优化”。

### Step 0：项目基础配置

- 搭建Gradle基础项目，配置依赖库（Retrofit、MPAndroidChart、Room等）。
- 初始化架构（如MVVM、模块包结构）。

### Step 1：行情卡片展示功能（P0）

- UI展示卡片（如你的附件图片风格）。
- 请求公开API（如CoinGecko），获取币种实时行情数据。
- 卡片中显示币名/简写、最新价格、涨幅、小K线走势。
- 编写数据模型与层级分离。

### Step 2：网络数据服务模块（P0）

- 集成Retrofit/OkHttp，封装币种行情和K线数据接口。
- 封装Repository层，支持数据缓存与错误兜底。
- 单元测试每个接口。

### Step 3：关注列表管理模块（P1）

- UI实现自选币种添加、删除、排序。
- 数据本地化存储，建议用Room或Jetpack DataStore。
- 自选币同步行情刷新。

### Step 4：K线图详情页模块（P1）

- 使用MPAndroidChart渲染K线，界面跳转和交互。
- 支持分钟/小时/日级别K线切换。
- 多币切换和历史数据拉取。

### Step 5：桌面小组件模块（P2）

- 使用AppWidgetProvider/Glance实现桌面Widget。
- 配置周期性刷新（updatePeriodMillis, WorkManager等）。
- 显示自选币种行情和小K线。

---

## 5. 关键代码/结构说明

### 5.1 目录推荐

/app
/data // 数据/网络/本地存储
/ui // UI 组件、Activity、Compose
/widget // 桌面小组件
/model // 实体Bean
/repo // 数据仓库


### 5.2 接口与业务

- DataRepository：行情API、K线API封装，缓存合并
- WidgetHelper：处理Widget刷新/数据适配
- FavoriteManager：自选管理与持久化封装

---

## 6. 建议及注意事项

- 所有模块接口要解耦，可根据需要扩展切换数据源。
- 桌面Widget刷新策略需兼顾电池性能与时效性。
- UI风格应简洁清晰，便于不同屏幕兼容。

---

## 7. 代码/开发进度管理

- 优先开发P0模块，确保App能初步可用。
- 每个功能开发完成后建议写单元测试。
- 模块上线后及时整理文档注释。

---

## 8. 参考资料/库/API

- MPAndroidChart: https://github.com/PhilJay/MPAndroidChart
- CoinGecko API: https://www.coingecko.com/en/api
- Widget开发文档: https://developer.android.com/develop/ui/views/appwidgets
- 网络库: Retrofit https://square.github.io/retrofit/
