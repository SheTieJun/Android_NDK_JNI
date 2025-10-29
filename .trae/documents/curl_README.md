# CurlHttp 网络请求库

**版本：** v2.0.0  
**更新日期：** 2024年12月  
**适用平台：** Android NDK

## 概述

CurlHttp 是一个基于 libcurl 的高性能 Android 网络请求库，提供完整的 HTTP 客户端功能。通过 JNI 调用原生 C++ 实现，确保卓越的性能和稳定性。

## 核心特性

### 🚀 高性能网络请求
- **原生实现**：基于 libcurl 的 C++ 实现，性能卓越
- **多协议支持**：HTTP/HTTPS、HTTP/2、FTP 等
- **连接复用**：自动管理连接池，提高请求效率
- **内存优化**：高效的内存管理和资源释放

### 🔧 完整的 HTTP 方法支持
- **GET**：获取资源
- **POST**：提交数据，支持 JSON、表单等格式
- **PUT**：更新资源
- **DELETE**：删除资源
- **HEAD**：获取响应头信息
- **PATCH**：部分更新资源
- **OPTIONS**：获取服务器支持的方法

### 🛡️ 高级功能
- **请求/响应拦截器**：支持自定义请求和响应处理
- **智能重试机制**：支持指数退避的重试策略
- **多种缓存策略**：CACHE_FIRST、NETWORK_FIRST、CACHE_ONLY 等
- **异步请求支持**：基于 Kotlin 协程的异步处理
- **SSL/TLS 支持**：完整的 HTTPS 支持和证书验证
- **文件上传下载**：支持大文件的分块传输

## 快速开始

### 1. 初始化库

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 初始化 CurlHttp
        CurlHttp.initCurl()
        
        // 可选：配置全局设置
        CurlHttp.setGlobalTimeout(30)
        CurlHttp.setGlobalConnectTimeout(10)
        CurlHttp.setGlobalUserAgent("MyApp/1.0")
    }
    
    override fun onTerminate() {
        super.onTerminate()
        // 清理资源
        CurlHttp.cleanUp()
    }
}
```

### 2. 基础用法

```kotlin
// GET 请求
val response = CurlHttp.get("https://api.example.com/users")
if (response.isSuccess) {
    println("Response: ${response.body}")
    println("Status: ${response.statusCode}")
}

// POST JSON 请求
val json = """{"name": "John", "email": "john@example.com"}"""
val response = CurlHttp.postJson("https://api.example.com/users", json)

// 带自定义头部的请求
val headers = mapOf(
    "Authorization" to "Bearer your-token",
    "Content-Type" to "application/json"
)
val response = CurlHttp.get("https://api.example.com/protected", headers)
```

## 详细 API 文档

### HttpRequest 对象

```kotlin
data class HttpRequest(
    val url: String,                    // 请求URL
    val method: HttpMethod = HttpMethod.GET,  // HTTP方法
    val headers: Map<String, String> = emptyMap(),  // 请求头
    val body: String? = null,           // 请求体
    val timeout: Int = 30,              // 超时时间（秒）
    val connectTimeout: Int = 10,       // 连接超时（秒）
    val followRedirects: Boolean = true, // 是否跟随重定向
    val ignoreSSL: Boolean = false,     // 是否忽略SSL证书验证
    val userAgent: String? = null,      // User-Agent
    val retryConfig: RetryConfig? = null, // 重试配置
    val cacheConfig: CacheConfig? = null  // 缓存配置
)
```

### HttpResponse 对象

```kotlin
data class HttpResponse(
    val statusCode: Int,                // HTTP状态码
    val statusMessage: String,          // 状态消息
    val headers: Map<String, String>,   // 响应头
    val body: String,                   // 响应体
    val responseTime: Long,             // 响应时间（毫秒）
    val fromCache: Boolean = false,     // 是否来自缓存
    val isSuccess: Boolean              // 是否成功（200-299）
) {
    val contentType: String?
        get() = headers["Content-Type"]
    
    val contentLength: Long
        get() = headers["Content-Length"]?.toLongOrNull() ?: -1L
}
```

### 基础请求方法

```kotlin
// GET 请求
fun get(url: String, headers: Map<String, String> = emptyMap()): HttpResponse

// POST 请求
fun post(url: String, body: String, headers: Map<String, String> = emptyMap()): HttpResponse

// POST JSON 请求
fun postJson(url: String, json: String, headers: Map<String, String> = emptyMap()): HttpResponse

// POST 表单请求
fun postForm(url: String, formData: Map<String, String>, headers: Map<String, String> = emptyMap()): HttpResponse

// PUT 请求
fun put(url: String, body: String, headers: Map<String, String> = emptyMap()): HttpResponse

// DELETE 请求
fun delete(url: String, headers: Map<String, String> = emptyMap()): HttpResponse

// HEAD 请求
fun head(url: String, headers: Map<String, String> = emptyMap()): HttpResponse

// 通用执行方法
fun execute(request: HttpRequest): HttpResponse
```

### 异步请求方法

```kotlin
// 异步执行请求
suspend fun executeAsync(request: HttpRequest): HttpResponse

// 异步 GET 请求
suspend fun getAsync(url: String, headers: Map<String, String> = emptyMap()): HttpResponse

// 异步 POST 请求
suspend fun postAsync(url: String, body: String, headers: Map<String, String> = emptyMap()): HttpResponse

// 使用示例
lifecycleScope.launch {
    try {
        val response = CurlHttp.getAsync("https://api.example.com/data")
        // 处理响应
        updateUI(response.body)
    } catch (e: HttpException) {
        // 处理HTTP异常
        Log.e("Network", "HTTP Error: ${e.statusCode}")
    } catch (e: Exception) {
        // 处理其他异常
        Log.e("Network", "Error: ${e.message}")
    }
}
```

## 高级功能

### 拦截器

#### 请求拦截器

```kotlin
interface RequestInterceptor {
    fun intercept(request: HttpRequest): HttpRequest
}

// 添加认证头的拦截器
val authInterceptor = object : RequestInterceptor {
    override fun intercept(request: HttpRequest): HttpRequest {
        return request.copy(
            headers = request.headers + ("Authorization" to "Bearer $token")
        )
    }
}

// 添加公共参数的拦截器
val commonParamsInterceptor = object : RequestInterceptor {
    override fun intercept(request: HttpRequest): HttpRequest {
        val newUrl = if (request.url.contains("?")) {
            "${request.url}&version=1.0&platform=android"
        } else {
            "${request.url}?version=1.0&platform=android"
        }
        return request.copy(url = newUrl)
    }
}

// 注册拦截器
CurlHttp.addRequestInterceptor(authInterceptor)
CurlHttp.addRequestInterceptor(commonParamsInterceptor)
```

#### 响应拦截器

```kotlin
interface ResponseInterceptor {
    fun intercept(response: HttpResponse): HttpResponse
}

// 日志拦截器
val loggingInterceptor = object : ResponseInterceptor {
    override fun intercept(response: HttpResponse): HttpResponse {
        Log.d("CurlHttp", "Response: ${response.statusCode} - ${response.responseTime}ms")
        if (BuildConfig.DEBUG) {
            Log.d("CurlHttp", "Body: ${response.body}")
        }
        return response
    }
}

// 错误处理拦截器
val errorHandlingInterceptor = object : ResponseInterceptor {
    override fun intercept(response: HttpResponse): HttpResponse {
        if (response.statusCode == 401) {
            // 处理认证失败
            refreshToken()
        }
        return response
    }
}

// 注册拦截器
CurlHttp.addResponseInterceptor(loggingInterceptor)
CurlHttp.addResponseInterceptor(errorHandlingInterceptor)
```

### 重试机制

```kotlin
data class RetryConfig(
    val maxRetries: Int = 3,                    // 最大重试次数
    val retryDelay: Long = 1000,                // 重试延迟（毫秒）
    val backoffMultiplier: Float = 2.0f,        // 退避倍数
    val retryOnConnectionFailure: Boolean = true, // 连接失败时重试
    val retryOnTimeout: Boolean = true,         // 超时时重试
    val retryOnServerError: Boolean = false,    // 服务器错误时重试
    val retryCondition: ((HttpResponse) -> Boolean)? = null // 自定义重试条件
)

// 配置重试
val retryConfig = RetryConfig(
    maxRetries = 3,
    retryDelay = 1000,
    backoffMultiplier = 2.0f,
    retryOnConnectionFailure = true,
    retryOnTimeout = true,
    retryCondition = { response ->
        // 自定义重试条件：5xx错误或特定4xx错误
        response.statusCode >= 500 || response.statusCode == 429
    }
)

// 设置全局重试配置
CurlHttp.setDefaultRetryConfig(retryConfig)

// 或者为单个请求设置
val request = HttpRequest(
    url = "https://api.example.com/data",
    retryConfig = retryConfig
)
```

### 缓存策略

```kotlin
enum class CacheStrategy {
    CACHE_FIRST,    // 优先使用缓存
    NETWORK_FIRST,  // 优先使用网络
    CACHE_ONLY,     // 仅使用缓存
    NETWORK_ONLY    // 仅使用网络
}

data class CacheConfig(
    val strategy: CacheStrategy = CacheStrategy.NETWORK_FIRST,
    val maxAge: Int = 300,              // 缓存最大存活时间（秒）
    val maxSize: Long = 10 * 1024 * 1024, // 缓存最大大小（字节）
    val cacheKey: String? = null        // 自定义缓存键
)

// 配置缓存
val cacheConfig = CacheConfig(
    strategy = CacheStrategy.CACHE_FIRST,
    maxAge = 300, // 5分钟
    maxSize = 10 * 1024 * 1024 // 10MB
)

// 设置全局缓存配置
CurlHttp.setDefaultCacheConfig(cacheConfig)

// 为单个请求设置缓存
val request = HttpRequest(
    url = "https://api.example.com/data",
    cacheConfig = cacheConfig
)

// 缓存管理
CurlHttp.clearCache()                    // 清空所有缓存
CurlHttp.clearCache("specific-key")      // 清空特定缓存
val stats = CurlHttp.getCacheStats()     // 获取缓存统计
```

### 文件上传下载

```kotlin
// 文件上传
fun uploadFile(
    url: String,
    filePath: String,
    fieldName: String = "file",
    headers: Map<String, String> = emptyMap(),
    progressCallback: ((Long, Long) -> Unit)? = null
): HttpResponse

// 多文件上传
fun uploadFiles(
    url: String,
    files: Map<String, String>, // fieldName to filePath
    headers: Map<String, String> = emptyMap(),
    progressCallback: ((Long, Long) -> Unit)? = null
): HttpResponse

// 文件下载
fun downloadFile(
    url: String,
    savePath: String,
    headers: Map<String, String> = emptyMap(),
    progressCallback: ((Long, Long) -> Unit)? = null
): HttpResponse

// 使用示例
// 上传文件
val uploadResponse = CurlHttp.uploadFile(
    url = "https://api.example.com/upload",
    filePath = "/sdcard/image.jpg",
    fieldName = "image",
    progressCallback = { uploaded, total ->
        val progress = (uploaded * 100 / total).toInt()
        Log.d("Upload", "Progress: $progress%")
    }
)

// 下载文件
val downloadResponse = CurlHttp.downloadFile(
    url = "https://example.com/file.zip",
    savePath = "/sdcard/Download/file.zip",
    progressCallback = { downloaded, total ->
        val progress = (downloaded * 100 / total).toInt()
        Log.d("Download", "Progress: $progress%")
    }
)
```

## 错误处理

### 异常类型

```kotlin
// HTTP异常
class HttpException(
    val statusCode: Int,
    message: String,
    val response: HttpResponse? = null
) : Exception(message)

// 网络异常
class NetworkException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

// 超时异常
class TimeoutException(
    message: String
) : Exception(message)
```

### 错误处理示例

```kotlin
try {
    val response = CurlHttp.get("https://api.example.com/data")
    
    when {
        response.isSuccess -> {
            // 处理成功响应
            processResponse(response.body)
        }
        response.statusCode == 404 -> {
            // 处理资源不存在
            Log.w("Network", "Resource not found")
        }
        response.statusCode >= 500 -> {
            // 处理服务器错误
            Log.e("Network", "Server error: ${response.statusCode}")
        }
        else -> {
            // 处理其他HTTP错误
            Log.e("Network", "HTTP error: ${response.statusCode}")
        }
    }
    
} catch (e: HttpException) {
    // 处理HTTP异常
    Log.e("Network", "HTTP Exception: ${e.statusCode} - ${e.message}")
    when (e.statusCode) {
        401 -> handleUnauthorized()
        403 -> handleForbidden()
        429 -> handleRateLimit()
        else -> handleGenericHttpError(e)
    }
    
} catch (e: NetworkException) {
    // 处理网络异常
    Log.e("Network", "Network Exception: ${e.message}")
    handleNetworkError()
    
} catch (e: TimeoutException) {
    // 处理超时异常
    Log.e("Network", "Timeout Exception: ${e.message}")
    handleTimeout()
    
} catch (e: Exception) {
    // 处理其他异常
    Log.e("Network", "Unexpected error: ${e.message}")
    handleUnexpectedError(e)
}
```

## 配置和优化

### 全局配置

```kotlin
// 设置全局超时
CurlHttp.setGlobalTimeout(30)           // 总超时时间
CurlHttp.setGlobalConnectTimeout(10)    // 连接超时时间

// 设置全局User-Agent
CurlHttp.setGlobalUserAgent("MyApp/1.0 (Android)")

// 设置全局头部
CurlHttp.setGlobalHeaders(mapOf(
    "Accept" to "application/json",
    "Accept-Language" to "zh-CN,zh;q=0.9,en;q=0.8"
))

// 配置SSL
CurlHttp.setSSLVerification(true)       // 启用SSL验证
CurlHttp.setCABundle("/path/to/ca-bundle.crt") // 设置CA证书包

// 配置代理
CurlHttp.setProxy("http://proxy.example.com:8080")
CurlHttp.setProxyAuth("username", "password")

// 配置连接池
CurlHttp.setMaxConnections(10)          // 最大连接数
CurlHttp.setMaxConnectionsPerHost(5)    // 每个主机最大连接数
```

### 性能优化

```kotlin
// 启用HTTP/2
CurlHttp.enableHTTP2(true)

// 启用压缩
CurlHttp.enableCompression(true)

// 配置DNS缓存
CurlHttp.setDNSCacheTimeout(300) // 5分钟

// 启用TCP Keep-Alive
CurlHttp.enableKeepAlive(true)
CurlHttp.setKeepAliveIdle(60)    // 空闲时间
CurlHttp.setKeepAliveInterval(30) // 探测间隔

// 配置缓冲区大小
CurlHttp.setBufferSize(64 * 1024) // 64KB
```

### 调试和监控

```kotlin
// 启用详细日志
CurlHttp.setVerbose(true)

// 获取统计信息
val stats = CurlHttp.getStatistics()
Log.d("CurlHttp", "Total requests: ${stats.totalRequests}")
Log.d("CurlHttp", "Success rate: ${stats.successRate}%")
Log.d("CurlHttp", "Average response time: ${stats.averageResponseTime}ms")

// 获取缓存统计
val cacheStats = CurlHttp.getCacheStats()
Log.d("CurlHttp", "Cache hit rate: ${cacheStats.hitRate}%")
Log.d("CurlHttp", "Cache size: ${cacheStats.size} bytes")

// 重置统计
CurlHttp.resetStatistics()
```

## 最佳实践

### 1. 应用生命周期管理

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 在应用启动时初始化
        CurlHttp.initCurl()
        
        // 配置全局设置
        setupGlobalConfig()
        
        // 注册拦截器
        setupInterceptors()
    }
    
    override fun onTerminate() {
        super.onTerminate()
        // 在应用结束时清理资源
        CurlHttp.cleanUp()
    }
    
    private fun setupGlobalConfig() {
        CurlHttp.setGlobalTimeout(30)
        CurlHttp.setGlobalUserAgent("MyApp/${BuildConfig.VERSION_NAME}")
        CurlHttp.setDefaultRetryConfig(RetryConfig(maxRetries = 3))
    }
    
    private fun setupInterceptors() {
        // 添加认证拦截器
        CurlHttp.addRequestInterceptor(AuthInterceptor())
        
        // 添加日志拦截器（仅调试模式）
        if (BuildConfig.DEBUG) {
            CurlHttp.addResponseInterceptor(LoggingInterceptor())
        }
    }
}
```

### 2. 网络状态检查

```kotlin
fun makeRequest(url: String): HttpResponse? {
    // 检查网络状态
    if (!isNetworkAvailable()) {
        Log.w("Network", "No network available")
        return null
    }
    
    return try {
        CurlHttp.get(url)
    } catch (e: NetworkException) {
        Log.e("Network", "Network error: ${e.message}")
        null
    }
}

private fun isNetworkAvailable(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = connectivityManager.activeNetworkInfo
    return activeNetwork?.isConnectedOrConnecting == true
}
```

### 3. 请求去重

```kotlin
class RequestManager {
    private val ongoingRequests = mutableMapOf<String, Deferred<HttpResponse>>()
    
    suspend fun get(url: String): HttpResponse {
        // 检查是否有相同的请求正在进行
        ongoingRequests[url]?.let { return it.await() }
        
        // 创建新的请求
        val deferred = GlobalScope.async {
            try {
                CurlHttp.getAsync(url)
            } finally {
                ongoingRequests.remove(url)
            }
        }
        
        ongoingRequests[url] = deferred
        return deferred.await()
    }
}
```

### 4. 错误重试策略

```kotlin
suspend fun reliableRequest(request: HttpRequest, maxRetries: Int = 3): HttpResponse {
    var lastException: Exception? = null
    
    repeat(maxRetries) { attempt ->
        try {
            return CurlHttp.executeAsync(request)
        } catch (e: NetworkException) {
            lastException = e
            if (attempt < maxRetries - 1) {
                // 指数退避
                delay(1000L * (1 shl attempt))
            }
        } catch (e: HttpException) {
            // 对于某些HTTP错误不重试
            if (e.statusCode in 400..499 && e.statusCode != 429) {
                throw e
            }
            lastException = e
            if (attempt < maxRetries - 1) {
                delay(1000L * (1 shl attempt))
            }
        }
    }
    
    throw lastException ?: Exception("Request failed after $maxRetries attempts")
}
```

## 注意事项

⚠️ **重要提醒：**

1. **初始化和清理**：确保在应用启动时调用 `initCurl()`，在应用结束时调用 `cleanUp()`
2. **线程安全**：所有API都是线程安全的，可以在多线程环境中使用
3. **内存管理**：库会自动管理内存，但大量并发请求时注意监控内存使用
4. **网络权限**：确保在 AndroidManifest.xml 中添加网络权限
5. **HTTPS证书**：生产环境中务必启用SSL证书验证
6. **异常处理**：始终正确处理网络异常，提供良好的用户体验
7. **缓存策略**：合理使用缓存，避免不必要的网络请求
8. **超时设置**：根据实际网络环境调整超时时间

## 依赖要求

- **Android API Level**: 21+
- **NDK版本**: r21+
- **libcurl版本**: 7.80+
- **OpenSSL版本**: 1.1.1+

## 版本更新日志

### v2.0.0 (2024年12月)
- ✨ **新增功能**：完整的HTTP客户端功能
- 🚀 **性能优化**：连接复用和内存管理优化
- 🛡️ **安全增强**：完整的SSL/TLS支持
- 🔧 **API重构**：更简洁易用的API设计
- 📱 **异步支持**：基于Kotlin协程的异步请求
- 🎯 **拦截器系统**：灵活的请求/响应拦截机制
- 💾 **缓存系统**：多种缓存策略支持
- 🔄 **重试机制**：智能重试和错误处理
- 📊 **监控统计**：详细的性能统计和监控
- 📚 **文档完善**：全面的API文档和使用指南

### v1.0.0
- 基础HTTP请求功能
- 简单的GET/POST支持

## 技术支持

如果在使用过程中遇到问题，请：

1. 查阅本文档的相关章节
2. 检查日志输出获取详细错误信息
3. 确认网络连接和权限配置
4. 验证请求参数和URL格式
5. 联系技术支持团队

---

*本文档持续更新中，如有疑问请及时反馈。*