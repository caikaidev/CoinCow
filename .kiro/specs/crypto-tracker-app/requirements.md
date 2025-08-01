# 加密货币行情App需求文档

## 项目简介

开发一款面向普通用户的加密货币行情追踪App，重点关注简单易用的用户体验。用户可以通过主应用和桌面Widget实时查看主流加密货币及SOL生态meme币的价格走势，无需复杂的交易功能，专注于行情展示和追踪。

## 需求列表

### 需求1：实时行情展示

**用户故事：** 作为普通用户，我希望能够查看主流加密货币的实时价格信息，以便了解市场动态。

#### 验收标准

1. WHEN 用户打开应用 THEN 系统 SHALL 显示预设的主流币种列表（BTC、ETH、BNB、SOL等）
2. WHEN 系统获取行情数据 THEN 系统 SHALL 显示币种名称、当前价格、24小时涨跌幅、小型价格走势图
3. WHEN 网络连接正常 THEN 系统 SHALL 每1分钟自动更新一次价格数据
4. IF API支持WebSocket连接 THEN 系统 SHALL 优先使用WebSocket实现更实时的数据更新
5. WHEN 网络请求失败 THEN 系统 SHALL 显示友好的错误提示信息

### 需求2：自选币种管理

**用户故事：** 作为用户，我希望能够添加和管理自己关注的币种，以便专注于感兴趣的投资标的。

#### 验收标准

1. WHEN 用户点击添加按钮 THEN 系统 SHALL 显示可搜索的币种列表
2. WHEN 用户搜索币种 THEN 系统 SHALL 支持按名称或代码模糊搜索主流币种和SOL生态meme币
3. WHEN 用户添加币种到自选 THEN 系统 SHALL 将该币种保存到本地存储
4. WHEN 用户删除自选币种 THEN 系统 SHALL 从自选列表和本地存储中移除该币种
5. WHEN 应用重启 THEN 系统 SHALL 自动加载用户的自选币种列表

### 需求3：K线图详情展示

**用户故事：** 作为用户，我希望查看币种的详细价格走势图，以便更好地分析价格趋势。

#### 验收标准

1. WHEN 用户点击币种卡片 THEN 系统 SHALL 跳转到该币种的详情页面
2. WHEN 详情页加载 THEN 系统 SHALL 显示该币种的K线图（默认24小时）
3. WHEN 用户切换时间周期 THEN 系统 SHALL 支持1小时、24小时、7天、30天的K线图切换
4. WHEN 显示K线图 THEN 系统 SHALL 包含价格、成交量等基本信息
5. WHEN 用户滑动K线图 THEN 系统 SHALL 支持缩放和平移操作

### 需求4：桌面Widget功能

**用户故事：** 作为用户，我希望在手机桌面上直接查看关注币种的价格，无需打开应用即可了解行情变化。

#### 验收标准

1. WHEN 用户添加Widget到桌面 THEN 系统 SHALL 显示用户自选币种的价格信息
2. WHEN Widget显示行情 THEN 系统 SHALL 包含币种名称、当前价格、涨跌幅信息
3. WHEN 系统更新数据 THEN Widget SHALL 每1分钟自动刷新显示内容
4. WHEN 用户点击Widget THEN 系统 SHALL 打开对应币种的详情页面
5. IF 用户未设置自选币种 THEN Widget SHALL 显示默认的主流币种（BTC、ETH、SOL）
6. WHEN 网络不可用 THEN Widget SHALL 显示上次缓存的数据和网络状态提示

### 需求5：数据源管理

**用户故事：** 作为开发者，我希望系统具有灵活的数据源架构，以便未来可以方便地切换或扩展API提供商。

#### 验收标准

1. WHEN 系统初始化 THEN 系统 SHALL 使用免费API（如CoinGecko）作为默认数据源
2. WHEN API请求失败 THEN 系统 SHALL 实现重试机制和降级策略
3. WHEN 需要切换数据源 THEN 系统 SHALL 通过配置文件或接口抽象层实现无缝切换
4. WHEN API达到限流阈值 THEN 系统 SHALL 实现请求队列和频率控制
5. WHEN 系统检测到数据异常 THEN 系统 SHALL 记录日志并提供数据验证机制

### 需求6：用户界面体验

**用户故事：** 作为普通用户，我希望应用界面简洁直观，操作简单易懂。

#### 验收标准

1. WHEN 用户首次使用 THEN 系统 SHALL 提供简单的引导说明
2. WHEN 数据加载中 THEN 系统 SHALL 显示加载状态指示器
3. WHEN 用户下拉刷新 THEN 系统 SHALL 手动触发数据更新
4. WHEN 系统检测到暗黑模式 THEN 系统 SHALL 自动适配暗黑主题
5. WHEN 用户操作界面 THEN 系统 SHALL 提供适当的触觉反馈和动画效果
6. WHEN 发生错误 THEN 系统 SHALL 显示用户友好的错误信息而非技术错误码