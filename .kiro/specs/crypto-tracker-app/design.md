# 加密货币行情App技术设计文档

## 概述

本设计文档基于需求文档，为加密货币行情App提供详细的技术架构和实现方案。系统采用现代Android开发技术栈，重点关注简洁的用户体验、灵活的数据源架构和高效的Widget实现。

## 架构设计

### 整体架构

采用Clean Architecture + MVVM模式，确保代码的可测试性和可维护性：

```
┌─────────────────────────────────────────┐
│                UI Layer                 │
│  (Compose UI + ViewModels + States)     │
├─────────────────────────────────────────┤
│              Domain Layer               │
│     (Use Cases + Repository Interface)  │
├─────────────────────────────────────────┤
│               Data Layer                │
│  (Repository Impl + API + Local Cache)  │
└─────────────────────────────────────────┘
```

### 核心技术栈

- **UI框架**: Jetpack Compose + Material Design 3 (Instagram风格定制)
- **状态管理**: ViewModel + StateFlow + Compose State
- **网络层**: Retrofit + OkHttp + Moshi
- **本地存储**: DataStore (Preferences) + Room (缓存)
- **异步处理**: Kotlin Coroutines + Flow
- **依赖注入**: Hilt
- **图表库**: Vico (Google官方Compose图表库)
- **Widget**: Glance (Compose for Widgets)
- **图片加载**: Coil (支持圆角和渐变效果)
- **动画**: Compose Animation API (流畅的转场效果)

## 组件和接口设计

### 1. 数据层 (Data Layer)

#### API接口抽象
```kotlin
interface CryptoDataSource {
    suspend fun getMarketData(coinIds: List<String>): Result<List<CoinMarketData>>
    suspend fun getCoinDetails(coinId: String): Result<CoinDetails>
    suspend fun getHistoricalData(coinId: String, days: Int): Result<List<PricePoint>>
    suspend fun searchCoins(query: String): Result<List<CoinSearchResult>>
}

// 具体实现
class CoinGeckoDataSource : CryptoDataSource
class BinanceDataSource : CryptoDataSource  // 未来扩展
```

#### Repository模式
```kotlin
interface CryptoRepository {
    fun getWatchlistCoins(): Flow<List<CoinMarketData>>
    fun getCoinDetails(coinId: String): Flow<CoinDetails>
    fun getHistoricalData(coinId: String, period: TimePeriod): Flow<List<PricePoint>>
    suspend fun addToWatchlist(coinId: String)
    suspend fun removeFromWatchlist(coinId: String)
    suspend fun searchCoins(query: String): List<CoinSearchResult>
}
```

#### 数据模型
```kotlin
@Serializable
data class CoinMarketData(
    val id: String,
    val symbol: String,
    val name: String,
    val currentPrice: Double,
    val priceChangePercentage24h: Double,
    val sparklineIn7d: List<Double>?,
    val lastUpdated: Long
)

@Serializable
data class CoinDetails(
    val id: String,
    val name: String,
    val symbol: String,
    val currentPrice: Double,
    val marketCap: Long,
    val volume24h: Long,
    val priceChangePercentage24h: Double
)
```

### 2. 领域层 (Domain Layer)

#### 用例定义
```kotlin
class GetWatchlistUseCase(private val repository: CryptoRepository)
class AddCoinToWatchlistUseCase(private val repository: CryptoRepository)
class GetCoinDetailsUseCase(private val repository: CryptoRepository)
class SearchCoinsUseCase(private val repository: CryptoRepository)
class RefreshMarketDataUseCase(private val repository: CryptoRepository)
```

### 3. UI层 (Presentation Layer)

#### 主要Screen组件
```kotlin
@Composable
fun WatchlistScreen(viewModel: WatchlistViewModel)

@Composable
fun CoinDetailScreen(coinId: String, viewModel: CoinDetailViewModel)

@Composable
fun SearchScreen(viewModel: SearchViewModel)
```

#### ViewModel设计
```kotlin
class WatchlistViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(WatchlistUiState())
    val uiState: StateFlow<WatchlistUiState> = _uiState.asStateFlow()
    
    fun refreshData()
    fun addCoin(coinId: String)
    fun removeCoin(coinId: String)
}

data class WatchlistUiState(
    val coins: List<CoinMarketData> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
```

### 4. Widget组件

#### Glance Widget实现
```kotlin
class CryptoWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            CryptoWidgetContent()
        }
    }
}

@Composable
fun CryptoWidgetContent() {
    val prefs = currentState<Preferences>()
    val coinData = prefs[coinDataKey] ?: emptyList()
    
    LazyColumn {
        items(coinData) { coin ->
            CoinWidgetItem(coin)
        }
    }
}
```

## 数据模型设计

### 本地存储结构

#### DataStore配置
```kotlin
// 用户偏好设置
data class UserPreferences(
    val watchlistCoinIds: Set<String> = emptySet(),
    val refreshInterval: Long = 60_000L, // 1分钟
    val isDarkMode: Boolean = false
)
```

#### Room缓存实体
```kotlin
@Entity(tableName = "coin_cache")
data class CoinCacheEntity(
    @PrimaryKey val id: String,
    val data: String, // JSON序列化的币种数据
    val timestamp: Long
)
```

### 网络数据模型

#### CoinGecko API响应模型
```kotlin
@Serializable
data class CoinGeckoMarketResponse(
    val id: String,
    val symbol: String,
    val name: String,
    @SerialName("current_price") val currentPrice: Double,
    @SerialName("price_change_percentage_24h") val priceChangePercentage24h: Double,
    @SerialName("sparkline_in_7d") val sparklineIn7d: SparklineData?
)

@Serializable
data class SparklineData(
    val price: List<Double>
)
```

## 错误处理策略

### 网络错误处理
```kotlin
sealed class NetworkResult<T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error<T>(val exception: Exception) : NetworkResult<T>()
    data class Loading<T> : NetworkResult<T>()
}

class NetworkErrorHandler {
    fun handleApiError(throwable: Throwable): String {
        return when (throwable) {
            is HttpException -> handleHttpError(throwable.code())
            is IOException -> "网络连接异常，请检查网络设置"
            is JsonDataException -> "数据解析错误"
            else -> "未知错误，请稍后重试"
        }
    }
}
```

### API限流处理
```kotlin
class RateLimitInterceptor : Interceptor {
    private val requestQueue = ArrayDeque<Long>()
    private val maxRequestsPerMinute = 50
    
    override fun intercept(chain: Interceptor.Chain): Response {
        // 实现请求频率控制逻辑
        waitIfNecessary()
        return chain.proceed(chain.request())
    }
}
```

## 测试策略

### 单元测试
- Repository层测试：使用MockWebServer模拟API响应
- ViewModel测试：使用TestCoroutineDispatcher测试异步逻辑
- UseCase测试：验证业务逻辑正确性

### UI测试
- Compose UI测试：使用ComposeTestRule测试UI交互
- Widget测试：使用GlanceAppWidgetHostRule测试Widget显示

### 集成测试
- API集成测试：验证真实API调用
- 数据库测试：使用Room的in-memory数据库

## 性能优化

### 数据缓存策略
```kotlin
class CacheStrategy {
    companion object {
        const val MARKET_DATA_CACHE_DURATION = 60_000L // 1分钟
        const val COIN_DETAILS_CACHE_DURATION = 300_000L // 5分钟
        const val SEARCH_CACHE_DURATION = 600_000L // 10分钟
    }
}
```

### 内存优化
- 使用Coil进行图片加载和缓存
- LazyColumn实现列表虚拟化
- 及时清理不需要的数据引用

### 网络优化
- 实现请求去重机制
- 使用OkHttp缓存减少重复请求
- 支持WebSocket连接以获得更实时的数据

## 安全考虑

### API密钥管理
```kotlin
// 在BuildConfig中安全存储API密钥
object ApiConfig {
    const val BASE_URL = "https://api.coingecko.com/api/v3/"
    val API_KEY: String = BuildConfig.COINGECKO_API_KEY
}
```

### 数据验证
```kotlin
class DataValidator {
    fun validatePriceData(price: Double): Boolean {
        return price > 0 && price.isFinite()
    }
    
    fun validatePercentageChange(percentage: Double): Boolean {
        return percentage.isFinite() && percentage > -100
    }
}
```

## UI设计风格指南 (Instagram风格)

### 设计原则
- **简洁现代**：采用Instagram式的简洁卡片设计，大量留白
- **圆角设计**：所有卡片、按钮使用统一的圆角半径(12dp)
- **渐变背景**：使用微妙的渐变色彩增强视觉层次
- **卡片阴影**：轻微的阴影效果营造层次感
- **流畅动画**：页面切换和状态变化使用平滑的动画过渡

### 色彩系统
```kotlin
object InstagramColors {
    // 主色调 - Instagram风格渐变
    val PrimaryGradient = listOf(
        Color(0xFF833AB4), // 紫色
        Color(0xFFE1306C), // 粉色  
        Color(0xFFFD1D1D)  // 红色
    )
    
    // 背景色
    val BackgroundLight = Color(0xFFFAFAFA)
    val BackgroundDark = Color(0xFF121212)
    
    // 卡片背景
    val CardBackground = Color(0xFFFFFFFF)
    val CardBackgroundDark = Color(0xFF1E1E1E)
    
    // 文字颜色
    val TextPrimary = Color(0xFF262626)
    val TextSecondary = Color(0xFF8E8E93)
    
    // 涨跌颜色
    val PriceUp = Color(0xFF00C851)   // 绿色
    val PriceDown = Color(0xFFFF4444) // 红色
}
```

### 组件设计规范
```kotlin
@Composable
fun InstagramStyleCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        content()
    }
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .background(
                brush = Brush.horizontalGradient(InstagramColors.PrimaryGradient),
                shape = RoundedCornerShape(25.dp)
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(25.dp)
    ) {
        Text(text, color = Color.White)
    }
}
```

### 布局设计
- **卡片间距**：16dp垂直间距，16dp水平边距
- **内容边距**：卡片内部16dp padding
- **圆角半径**：统一使用12dp圆角
- **字体层级**：标题18sp，副标题14sp，正文16sp
- **图标尺寸**：24dp标准图标，32dp重要操作图标

## 部署和配置

### Gradle配置
```kotlin
android {
    compileSdk 34
    
    defaultConfig {
        minSdk 24
        targetSdk 34
        
        buildConfigField("String", "COINGECKO_API_KEY", "\"${project.findProperty("COINGECKO_API_KEY")}\"")
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}
```

### 依赖管理
```kotlin
dependencies {
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    
    // Core Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    
    // ViewModel & Navigation
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.navigation:navigation-compose:2.7.6")
    
    // Network
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    
    // DI
    implementation("com.google.dagger:hilt-android:2.48")
    
    // Charts
    implementation("com.patrykandpatrick.vico:compose:1.13.1")
    
    // Widget
    implementation("androidx.glance:glance-appwidget:1.0.0")
}
```