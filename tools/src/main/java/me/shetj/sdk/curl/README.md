# CurlHttp - Android NDK网络请求库

## 概述

CurlHttp是基于libcurl的Android NDK网络请求库，提供了完整的HTTP客户端功能，支持多种HTTP方法、请求拦截器、响应拦截器、缓存机制、重试机制等高级功能。

## 特性

### 基础功能
- ✅ 支持GET、POST、PUT、DELETE、HEAD、PATCH、OPTIONS等HTTP方法
- ✅ 支持自定义请求头(Headers)
- ✅ 支持设置请求超时时间和连接超时时间
- ✅ 支持处理响应状态码和响应头
- ✅ 支持JSON请求和响应处理
- ✅ 支持HTTPS请求和证书验证

### 高级功能
- ✅ 请求拦截器和响应拦截器
- ✅ 智能重试机制（支持指数退避）
- ✅ 多种缓存策略
- ✅ 异步请求支持（Kotlin协程）
- ✅ 连接池管理
- ✅ 内存管理优化

### 性能优化
- ✅ 连接复用减少开销
- ✅ 请求结果缓存
- ✅ 内存泄漏防护
- ✅ 性能监控和统计

## 快速开始

### 初始化

```kotlin
// 初始化curl库
CurlHttp.initCurl()

// 应用退出时清理资源
CurlHttp.cleanUp()
```

### 基础用法

#### GET请求
```kotlin
// 简单GET请求
val response = CurlHttp.get("https://api.example.com/data")
println("Response: ${response.body}")

// 带自定义头部的GET请求
val headers = mapOf("Authorization" to "Bearer token")
val response = CurlHttp.get("https://api.example.com/data", headers)
```

#### POST请求
```kotlin
// JSON POST请求
val json = """{"name": "John", "age": 30}"""
val response = CurlHttp.postJson("https://api.example.com/users", json)

// 带自定义头部的POST请求
val headers = mapOf("Content-Type" to "application/json")
val response = CurlHttp.postJson("https://api.example.com/users", json, headers)
```

#### 其他HTTP方法
```kotlin
// PUT请求
val response = CurlHttp.putJson("https://api.example.com/users/1", json)

// DELETE请求
val response = CurlHttp.delete("https://api.example.com/users/1")
```

### 高级用法

#### 使用HttpRequest对象
```kotlin
val request = HttpRequest(
    url = "https://api.example.com/data",
    method = HttpMethod.GET,
    headers = mapOf("User-Agent" to "MyApp/1.0"),
    timeout = 30,
    connectTimeout = 10,
    ignoreSSL = false
)

val response = CurlHttp.execute(request)
```

#### 异步请求
```kotlin
// 使用协程进行异步请求
lifecycleScope.launch {
    try {
        val response = CurlHttp.executeAsync(request)
        // 处理响应
        println("Response: ${response.body}")
    } catch (e: Exception) {
        // 处理错误
        println("Error: ${e.message}")
    }
}
```

#### 请求拦截器
```kotlin
// 添加请求拦截器
val requestInterceptor = object : RequestInterceptor {
    override fun intercept(request: HttpRequest): HttpRequest {
        return request.copy(
            headers = request.headers + ("X-API-Key" to "your-api-key")
        )
    }
}

CurlHttp.addRequestInterceptor(requestInterceptor)
```

#### 响应拦截器
```kotlin
// 添加响应拦截器
val responseInterceptor = object : ResponseInterceptor {
    override fun intercept(response: HttpResponse): HttpResponse {
        // 记录响应时间
        Log.d("CurlHttp", "Response time: ${response.responseTime}ms")
        return response
    }
}

CurlHttp.addResponseInterceptor(responseInterceptor)
```

#### 配置重试机制
```kotlin
val retryConfig = RetryConfig(
    maxRetries = 3,
    retryDelay = 1000, // 1秒
    backoffMultiplier = 2.0f, // 指数退避
    retryOnConnectionFailure = true,
    retryOnTimeout = true
)

CurlHttp.setDefaultRetryConfig(retryConfig)
```

#### 配置缓存策略
```kotlin
val cacheConfig = CacheConfig(
    strategy = CacheStrategy.CACHE_FIRST,
    maxAge = 300, // 5分钟
    maxSize = 10 * 1024 * 1024 // 10MB
)

CurlHttp.setDefaultCacheConfig(cacheConfig)
```

## API文档

### CurlHttp主要方法

| 方法 | 描述 |
|------|------|
| `initCurl()` | 初始化curl库 |
| `cleanUp()` | 清理资源 |
| `execute(request)` | 执行HTTP请求（同步） |
| `executeAsync(request)` | 执行HTTP请求（异步） |
| `get(url, headers)` | GET请求 |
| `postJson(url, json, headers)` | POST JSON请求 |
| `putJson(url, json, headers)` | PUT JSON请求 |
| `delete(url, headers)` | DELETE请求 |

### 配置方法

| 方法 | 描述 |
|------|------|
| `setDefaultTimeout(timeout, connectTimeout)` | 设置默认超时时间 |
| `setDefaultRetryConfig(retryConfig)` | 设置默认重试配置 |
| `setDefaultCacheConfig(cacheConfig)` | 设置默认缓存配置 |
| `addRequestInterceptor(interceptor)` | 添加请求拦截器 |
| `addResponseInterceptor(interceptor)` | 添加响应拦截器 |

### 数据类

#### HttpRequest
```kotlin
data class HttpRequest(
    val url: String,
    val method: HttpMethod = HttpMethod.GET,
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null,
    val timeout: Int = 30,
    val connectTimeout: Int = 10,
    val userAgent: String? = null,
    val cookies: String? = null,
    val followRedirects: Boolean = true,
    val maxRedirects: Int = 5,
    val certificatePath: String? = null,
    val ignoreSSL: Boolean = false
)
```

#### HttpResponse
```kotlin
data class HttpResponse(
    val statusCode: Int,
    val statusMessage: String,
    val headers: Map<String, String> = emptyMap(),
    val body: String,
    val contentLength: Long = 0,
    val contentType: String? = null,
    val responseTime: Long = 0,
    val isSuccess: Boolean = statusCode in 200..299
)
```

## 缓存策略

| 策略 | 描述 |
|------|------|
| `NO_CACHE` | 不使用缓存 |
| `CACHE_FIRST` | 优先使用缓存 |
| `NETWORK_FIRST` | 优先使用网络 |
| `CACHE_ONLY` | 仅使用缓存 |
| `NETWORK_ONLY` | 仅使用网络 |

## 错误处理

```kotlin
try {
    val response = CurlHttp.get("https://api.example.com/data")
    if (response.isSuccess) {
        // 处理成功响应
        println("Success: ${response.body}")
    } else {
        // 处理HTTP错误
        println("HTTP Error: ${response.statusCode} - ${response.statusMessage}")
    }
} catch (e: HttpException) {
    // 处理HTTP异常
    println("HTTP Exception: ${e.statusCode} - ${e.message}")
} catch (e: Exception) {
    // 处理其他异常
    println("Error: ${e.message}")
}
```

## 性能监控

```kotlin
// 获取缓存统计信息
val cacheStats = CurlHttp.getCacheStats()
println("Cache stats: $cacheStats")

// 清空缓存
CurlHttp.clearCache()
```

## 证书配置

```kotlin
// 设置自定义证书
val certFile = File("/path/to/certificate.pem")
CurlHttp.setCertificate(certFile)
```

## 注意事项

1. **线程安全**: CurlHttp是线程安全的，可以在多线程环境中使用
2. **内存管理**: 库会自动管理内存，但建议在应用退出时调用`cleanUp()`
3. **网络权限**: 确保在AndroidManifest.xml中添加网络权限
4. **HTTPS**: 默认会验证SSL证书，可以通过`ignoreSSL`参数跳过验证（不推荐）

## 依赖要求

- Android API Level 21+
- NDK支持
- Kotlin协程（用于异步功能）

## 示例项目

查看项目中的测试用例了解更多使用方法：
- `CurlHttpTest.kt` - 完整的功能测试用例

## 更新日志

### v2.0.0
- 重构整个API，提供更好的类型安全
- 添加请求/响应拦截器支持
- 实现智能重试机制
- 添加多种缓存策略
- 支持异步请求（Kotlin协程）
- 完善错误处理机制
- 添加性能监控功能

### v1.0.0
- 基础HTTP请求功能
- 支持GET/POST方法
- 简单的SSL证书处理